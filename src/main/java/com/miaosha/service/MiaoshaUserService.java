package com.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.miaosha.dao.UserDao;
import com.miaosha.domain.Goods;
import com.miaosha.domain.MiaoshaGoods;
import com.miaosha.exception.GlobalException;
import com.miaosha.redis.GoodsKey;
import com.miaosha.result.Result;
import com.miaosha.vo.GoodsVo;
import com.miaosha.vo.RegistVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miaosha.dao.MiaoshaUserDao;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.MiaoshaUserKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.util.MD5Util;
import com.miaosha.util.UUIDUtil;
import com.miaosha.vo.LoginVo;

import java.util.Date;

@Service
public class MiaoshaUserService {
	
	
	public static final String COOKI_NAME_TOKEN = "token";
	public static final String SALT = "1a2b3c4d";

	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	//从数据库中根据id取用户
	//在缓存中存 id - user
	public MiaoshaUser getById(long id) {
		//取缓存
		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
		if(user != null) {
			return user;
		}
		//取数据库
		user = miaoshaUserDao.getById(id);
		if(user != null) {
			redisService.set(MiaoshaUserKey.getById, ""+id, user);
		}
		return user;
	}
	// http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323
	public boolean updatePassword(String token, long id, String formPass) {
		//取user
		MiaoshaUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		miaoshaUserDao.update(toBeUpdate);
		//处理缓存
		redisService.delete(MiaoshaUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MiaoshaUserKey.token, token, user);
		return true;
	}
	

	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
	

	public Result<String> login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie
		String token	 = UUIDUtil.uuid();
		addCookie(response, token, user);
		//如果是商家，返回商家登录成功信息
		if(user.getMerchant()>0){
			//redis中缓存 商家id(商品id) - token   为websocket中用户可以根据商品id在map中找到商家socket
			redisService.set(MiaoshaUserKey.merchant,""+user.getMerchant(),token);
			return Result.merchant_success();
		}else{
			return Result.success(token);
		}

	}
	/*
	 * @Author YX
	 * @Description
	 * @Date 17:00 2019/5/20
	 * @Param [response, token, user]
	 * @return void
	 *添加浏览器cookie
	 *添加  token - user缓存
	 **/

	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	/*
	 * @Author YX
	 * @Description
	 * 实现注册功能
	 * @Date 15:08 2019/5/24
	 * @Param [response, registVo]
	 * @return com.miaosha.result.Result<java.lang.String>
	 **/
    public Result<String> regist(HttpServletResponse response, RegistVo registVo) {
    	String mobile = registVo.getMobile();
    	MiaoshaUser user = miaoshaUserDao.getById(Long.parseLong(mobile));
    	if(user!=null){
    		return Result.error(CodeMsg.USER_EXIST);
		}
		Date date = new Date();
    	String salt = SALT;
    	String password = registVo.getPassword();
    	password = MD5Util.formPassToDBPass(password,salt);
    	MiaoshaUser registUser = new MiaoshaUser();
    	registUser.setId(Long.parseLong(mobile));
    	registUser.setNickname(registVo.getNickname());
    	registUser.setSalt(salt);
    	registUser.setPassword(password);
    	registUser.setRegisterDate(date);
    	registUser.setMerchant(0);
    	miaoshaUserDao.addUser(registUser);
    	return Result.success(Result.SUCCESS);
    }
//	private void addCookie_merchant(HttpServletResponse response, String token, GoodsVo goods) {
//		redisService.set(GoodsKey.GoodsIdToken, token, goods);
//		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
//		cookie.setMaxAge(GoodsKey.GoodsIdToken.expireSeconds());
//		cookie.setPath("/");
//		response.addCookie(cookie);
//	}
//
//	public String merchant_login(HttpServletResponse response, LoginVo loginVo) {
//		if(loginVo == null) {
//			throw new GlobalException(CodeMsg.SERVER_ERROR);
//		}
//		String goodsId = loginVo.getMobile();
//		//判断商家是否存在
//		GoodsVo good = getGooodsById(Long.parseLong(goodsId));
//		if(good == null) {
//			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
//		}
//		//生成cookie
//		String token	 = UUIDUtil.uuid();
//		addCookie_merchant(response, token, good);
//		return token;
//	}
//
//	private GoodsVo getGooodsById(long id) {
//		//取缓存
//		GoodsVo good = redisService.get(MiaoshaUserKey.getById, ""+id, GoodsVo.class);
//		if(good!= null) {
//			return good;
//		}
//		//取数据库
//		good = goodsService.getGoodsVoByGoodsId(id);
//		if(good != null) {
//			redisService.set(GoodsKey.GoodsId, ""+id, good);
//		}
//		return good;
//	}
}
