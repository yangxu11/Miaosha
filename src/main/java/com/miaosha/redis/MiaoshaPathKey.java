package com.miaosha.redis;

/**
 * 秒杀真实地址标签
 *
 * @author yx
 * @create 2019-03-30  16:29
 **/
public class MiaoshaPathKey extends BasePrefix{

    public MiaoshaPathKey(String prefix) {
        super(prefix);
    }

    public MiaoshaPathKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaPathKey getMiaoshaPath = new MiaoshaPathKey(100,"miaosha_path");
}
