package com.miaosha.redis;

/**
 * redis的websocket存储头
 *
 * @author yx
 * @create 2019-05-21  15:49
 **/
public class WebSocketKey extends BasePrefix{
    //过期时间为3天
    private static  final int EXPIRE_TIME = 3600*24*3;

    public WebSocketKey(String prefix) {
        super(prefix);
    }

    public WebSocketKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static WebSocketKey socket_msg = new WebSocketKey(EXPIRE_TIME,"msg");
}
