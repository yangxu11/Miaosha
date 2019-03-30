package com.miaosha.redis;

/**
 * redis缓存秒杀相关的前缀
 *
 * @author yx
 * @create 2019-03-30  17:08
 **/
public class MiaoshaKey extends BasePrefix{
    public MiaoshaKey(String prefix) {
        super(prefix);
    }

    public MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(60,"getMiaoshaVerifyCode");
}
