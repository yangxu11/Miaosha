package com.miaosha.websocket;

/**
 * websocket消息模板
 *
 * @author yx
 * @create 2019-05-20  14:55
 **/
public class SocketMessage {
    public String src;
    public String dst;
    public String msg;

    public SocketMessage(String src, String dst, String msg) {
        this.src = src;
        this.dst = dst;
        this.msg = msg;
    }

    public SocketMessage(String msg) {
        this.msg = msg;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "SocketMessage{" +
                "src='" + src + '\'' +
                ", dst='" + dst + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
