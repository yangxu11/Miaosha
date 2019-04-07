package com.miaosha.redis;

/**
 * redis缓存访问次数的前缀
 *
 * @author yx
 * @create 2019-04-07  16:47
 **/
public class AccessKey extends BasePrefix {
    public AccessKey(String prefix) {
        super(prefix);
    }

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey withExpireTime(int expireTime){
        return new AccessKey(expireTime,"access");
    }
}
