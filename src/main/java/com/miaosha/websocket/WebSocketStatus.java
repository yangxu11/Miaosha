package com.miaosha.websocket;

/**
 * websocket反馈消息规范
 *
 * @author yx
 * @create 2019-05-20  15:18
 **/
public class WebSocketStatus {
    //正反馈
    public static final SocketMessage SUCCESS_CONNECT = new SocketMessage("连接成功");
    public static final SocketMessage SUCCESS_SEND = new SocketMessage("发送成功");



    //负反馈
    public static final SocketMessage WRONG_MSG = new SocketMessage("接受消息有误，请重发");
    public static final SocketMessage EMPTY_DST = new SocketMessage("对方暂时不在，请稍后重试");
    public static final SocketMessage ERROR_CONNECT = new SocketMessage("连接失败，请重试");
}
