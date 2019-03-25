package com.miaosha.service;

import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miaosha.domain.MiaoshaUser;
import com.miaosha.domain.OrderInfo;
import com.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;

	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		if(success){
			//order_info maiosha_order
			return orderService.createOrder(user, goods);
		} else{
			redisService.set(GoodsKey.isGoodsOver,""+goods.getId(),true);
			return null;
		}
	}

	//查询订单是否存在，商品是否秒杀完
	public long getMiaoshaResult(Long id, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(id,goodsId);
		if(order!=null){
			return order.getOrderId();//成功
		} else {
			if(redisService.exists(GoodsKey.isGoodsOver,""+goodsId)){
				return -1;
			} else{
				return 0;
			}
		}
	}
}
