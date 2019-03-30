package com.miaosha.controller;

import com.miaosha.rabbitmq.MQSender;
import com.miaosha.rabbitmq.MiaoshaMessage;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.MiaoshaPathKey;
import com.miaosha.result.Result;
import com.miaosha.util.MD5Util;
import com.miaosha.util.UUIDUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.domain.OrderInfo;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	MQSender sender;

	private HashMap<Long, Boolean> localOverMap =  new HashMap<>();

	@Override
/*
*	系统初始化
 */
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> list = goodsService.listGoodsVo();
		if(list == null) {
			return;
		}
		for(GoodsVo goods : list){
			redisService.set(GoodsKey.getMiaoshaGoods,""+goods.getId() ,goods.getStockCount());
			localOverMap.put(goods.getId(),false);
		}
	}
	
	/**
	 * QPS:1306
	 * 5000 * 10
	 * */
	@RequestMapping(value="/{path}/do_miaosha",method= RequestMethod.POST)
	@ResponseBody
	public Result<Integer> list(MiaoshaUser user,
								@RequestParam("goodsId")long goodsId,
								@PathVariable("path")String path) {
		if(user == null) {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
		//判断加密的path是否正确
		String redis_path = redisService.get(MiaoshaPathKey.getMiaoshaPath,""+user.getId()+goodsId,String.class);
		if(!redis_path.equals(path)){
			return Result.error(CodeMsg.MIAOSHA_PATH_ERROR);
		}




		//判断库存
		//判断本地存的结束标签
		if(localOverMap.get(goodsId)){
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//预减库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoods, ""+goodsId);//10
		if(stock<0){
			localOverMap.put(goodsId,true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀过了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//加入消息队列
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.miaoshaSend(mm);
		//排队中
		return Result.success(0);
	}
	/**
	 * orderId：成功
	 * -1：秒杀失败
	 * 0： 排队中
	 * */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
									  @RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}

	/**
	 * @Author YX
	 * @Description 获取秒杀的真实地址,同时验证验证码
	 * @Date 16:27 2019/3/30
	 * @Param [model, user, goodsId]
	 * @return com.miaosha.result.Result<java.lang.Long>
	 **/
	@RequestMapping(value="/path", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> path(Model model, MiaoshaUser user,
									  @RequestParam("goodsId")long goodsId,
							   @RequestParam(value="verifyCode", defaultValue="0")int verifyCode){
		model.addAttribute("user", user);
		boolean check = miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
		if(!check){
			return Result.error(CodeMsg.MIAOSHA_VERIFICODE_ERROR);
		}
		String str = getPath(user,goodsId);
		return Result.success(str);
	}
	/**
	 * @Author YX
	 * @Description 根据user和goodId,以及uuid生成一个临时的访问路径前缀
	 * @Date 16:34 2019/3/30
	 * @Param [user, goodsId]
	 * @return java.lang.String
	 **/
	private String getPath(MiaoshaUser user, long goodsId) {
		String str = MD5Util.md5(UUIDUtil.uuid()+user.getId()+goodsId);
		redisService.set(MiaoshaPathKey.getMiaoshaPath,""+user.getId()+goodsId,str);
		return str;
	}

	/**
	 * @Author YX
	 * @Description  获取秒杀验证码
	 * @Date 17:04 2019/3/30
	 * @Param [response, user, goodsId]
	 * @return com.miaosha.result.Result<java.lang.String>
	 **/
	@RequestMapping(value="/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
											  @RequestParam("goodsId")long goodsId) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		try {
			BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		}catch(Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_FAIL);
		}
	}
}
