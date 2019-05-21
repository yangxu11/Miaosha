package com.miaosha.controller;

import com.miaosha.domain.MiaoshaUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 处理websocket请求
 *
 * @author yx
 * @create 2019-05-17  15:55
 **/
@Controller
@RequestMapping("/websocket")
public class WebSocketController {

    @RequestMapping("/connect")
    public void connect(MiaoshaUser user){

    }

}
