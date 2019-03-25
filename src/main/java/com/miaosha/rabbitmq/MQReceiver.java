package com.miaosha.rabbitmq;

import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.RedisService;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class MQReceiver {
    @Autowired
    RedisService redisService;
    @Autowired
    MiaoshaService miaoshaService;
    @Autowired
    GoodsService goodsService;
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @RabbitListener(queues = MQConfig.MIAOSHAQUEUE)
    public void miaoshaReceive(String message){
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(message,MiaoshaMessage.class);
        MiaoshaUser user = miaoshaMessage.getUser();
        long goodsId = miaoshaMessage.getGoodsId();

        //查询数据库中的商品数量
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock<=0) return;
        miaoshaService.miaosha(user,goods);
    }


    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("receive message from tq1" + message);
    }
    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("receive message from tq2" + message);
    }

    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    public void receiveHeader(String message){
        log.info("receive message from header" + message);
    }

}
