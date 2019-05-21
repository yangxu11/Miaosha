package com.miaosha.websocket;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaosha.redis.MiaoshaUserKey;
import com.miaosha.redis.RedisService;
import com.miaosha.service.OrderService;
import com.miaosha.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义websocket
 *
 * @author yx
 * @create 2019-05-17  16:23
 **/

@Component
@ServerEndpoint(value = "/websocket/{ip}")
public class MyWebSocket {

    //必须为静态，因为spring默认为单例模式，websocket为多对象，所有注入属性要为static
    private static RedisService redisService;

    //使用set方法注入
    @Autowired
    public void setRedisService(RedisService redisService){
        MyWebSocket.redisService = redisService;
    }

    private static final Logger log = LoggerFactory.getLogger(MyWebSocket.class);

    // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    // concurrent包的线程安全Map，用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentHashMap<String, MyWebSocket> webSocketMap = new ConcurrentHashMap<String, MyWebSocket>();

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private String ip; // 客户端ip
    public static final String ACTION_PRINT_ORDER = "printOrder";
    public static final String ACTION_SHOW_PRINT_EXPORT = "showPrintExport";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("ip") String ip) {
        this.session = session;
        //加入set中
        webSocketMap.put(ip,this);
        //在线数加1
        addOnlineCount();
        log.info("有新窗口开始监听:"+ip+",当前在线人数为" + getOnlineCount());
        this.ip = ip;

        this.sendObject(WebSocketStatus.SUCCESS_CONNECT);

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("ip") String ip) {
        // 从set中删除
        webSocketMap.remove(ip);
        // 在线数减1
        subOnlineCount();
        log.info("websocket关闭，IP：{},当前在线人数为:{}", ip, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * 将消息解析，消息分为三部分 {源IP；目的IP；消息内容}
     * 更加目的IP找到其响应的socket，向该socket发送消息
     * @param message
     *
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        //对收到的消息进行处理
        String[] strs = message.split(";");
        if(strs.length<3) {
            this.sendObject(WebSocketStatus.WRONG_MSG);
            return;
        }
        String src = strs[0];
        String msg = strs[2];
        //如果dst为goodsID,将其解析为商家的token
        String dst = strs[1];
        if(dst.length() == 0){
            this.sendObject(WebSocketStatus.EMPTY_DST);
            return;
        } else if(dst.length()!= UUIDUtil.LENGTH){
            dst = redisService.get(MiaoshaUserKey.merchant,dst,String.class);
            if(dst==null){
                this.sendObject(WebSocketStatus.EMPTY_DST);
                return;
            }
        }


        SocketMessage socketMessage = new SocketMessage(src,dst,msg);
        log.info("收到来自窗口"+ip+"的信息:"+socketMessage.toString());

        MyWebSocket socket = webSocketMap.get(dst);
        if(socket==null){
            this.sendObject(WebSocketStatus.EMPTY_DST);
            return;
        }
        socket.sendObject(socketMessage);
        this.sendObject(WebSocketStatus.SUCCESS_SEND);

    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("webSocket发生错误！IP：{}", ip);
        this.sendObject(WebSocketStatus.ERROR_CONNECT);
        error.printStackTrace();
    }

    /**
     * 像当前客户端发送消息
     *
     * @param message
     *            字符串消息
     * @throws IOException
     */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("发送数据错误，ip:{},msg：{}", ip, message);
        }
    }

    /**
     * 向当前客户端发送对象
     *
     * 先将对象转换成json字符串，再将字符串发送出去
     *
     * @param obj
     *            所发送对象
     * @throws IOException
     */
    public void sendObject(Object obj) {
        JSONObject json = (JSONObject) JSONObject.toJSON((obj));
        log.info(json.toString());
        this.sendMessage(json.toString());
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        String s = null;
//        try {
//            s = mapper.writeValueAsString(obj);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            log.error("转json错误！{}", obj);
//        }
//        this.sendMessage(s);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) {
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }

    public static ConcurrentHashMap<String, MyWebSocket> getWebSocketMap() {
        return webSocketMap;
    }


}
