package com.miaosha.controller;

import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.vo.LoginVo;
import com.miaosha.vo.RegistVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 处理注册请求
 *
 * @author yx
 * @create 2019-05-24  14:18
 **/

@Controller
@RequestMapping("/regist")
public class RegistController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    MiaoshaUserService userService;
    @Autowired
    RedisService redisService;

    @RequestMapping("/to_regist")
    public String toLogin() {
        return "regist";
    }

    @RequestMapping("/do_regist")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid RegistVo registVo) {
        log.info(registVo.toString());
        //注册
        return userService.regist(response, registVo);
    }
}
