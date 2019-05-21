package com.miaosha.redis;

public class GoodsKey extends BasePrefix{

	public static final int TOKEN_EXPIRE = 3600*24 * 2;

	private GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
	public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
	public static GoodsKey getMiaoshaGoods = new GoodsKey(0,"miaosha");
	public static GoodsKey isGoodsOver = new GoodsKey(0,"over");
	public static GoodsKey GoodsIdToken = new GoodsKey(TOKEN_EXPIRE,"good-tk");
	public static GoodsKey GoodsId = new GoodsKey(TOKEN_EXPIRE,"goodid");
}
