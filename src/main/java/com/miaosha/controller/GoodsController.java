package com.miaosha.controller;

import java.util.List;

import com.miaosha.redis.GoodsKey;
import com.miaosha.result.Result;
import com.miaosha.vo.GoodsDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.RedisService;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.vo.GoodsVo;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;

	@Autowired
	ApplicationContext applicationContext;
	
	/**
	 * QPS:1267
	 * 5000 * 10
	 * */
//    @RequestMapping(value="/to_list")
//    public String list(Model model,MiaoshaUser user) {
//    	model.addAttribute("user", user);
//    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
//    	model.addAttribute("goodsList", goodsList);
//    	 return "goods_list";
//    }
	@RequestMapping(value="/to_list")
	@ResponseBody
	public String list(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user) {
		model.addAttribute("user", user);
		//查看页面是否有缓存
		String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
		if(html != null){
			return html;
		}
		//没有缓存
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		//手动渲染
		SpringWebContext ctx = new SpringWebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap(), applicationContext);
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}
    
//    @RequestMapping("/to_detail/{goodsId}")
//    public String detail(Model model,MiaoshaUser user,
//    		@PathVariable("goodsId")long goodsId) {
//    	model.addAttribute("user", user);
//
//    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//    	model.addAttribute("goods", goods);
//
//    	long startAt = goods.getStartDate().getTime();
//    	long endAt = goods.getEndDate().getTime();
//    	long now = System.currentTimeMillis();
//
//    	int miaoshaStatus = 0;
//    	int remainSeconds = 0;
//    	if(now < startAt ) {//秒杀还没开始，倒计时
//    		miaoshaStatus = 0;
//    		remainSeconds = (int)((startAt - now )/1000);
//    	}else  if(now > endAt){//秒杀已经结束
//    		miaoshaStatus = 2;
//    		remainSeconds = -1;
//    	}else {//秒杀进行中
//    		miaoshaStatus = 1;
//    		remainSeconds = 0;
//    	}
//    	model.addAttribute("miaoshaStatus", miaoshaStatus);
//    	model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";
//    }

	@RequestMapping("/to_detail/{goodsId}")
	@ResponseBody
	public String detail(HttpServletResponse response,HttpServletRequest request,Model model,MiaoshaUser user,
						 @PathVariable("goodsId")long goodsId) {
		model.addAttribute("user", user);
		//取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
		if(html != null){
			return html;
		}
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);

		//手动渲染
		SpringWebContext ctx = new SpringWebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap(), applicationContext);
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
	}


	//页面静态化，前端页面直接跳转到目标HTML，由目标HTML向服务器请求动态数据，目标HTML可以在浏览器上缓存
	@RequestMapping("/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail2( MiaoshaUser user,
										 @PathVariable("goodsId")long goodsId) {
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
		goodsDetailVo.setGoods(goods);
		goodsDetailVo.setUser(user);
		goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
		goodsDetailVo.setRemainSeconds(remainSeconds);

		return Result.success(goodsDetailVo);
	}
    
}
