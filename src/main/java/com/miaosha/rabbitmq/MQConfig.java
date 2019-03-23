package com.miaosha.rabbitmq;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class MQConfig {
    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic-queue1";
    public static final String TOPIC_QUEUE2 = "topic-queue2";
    public static final String TOPIC_EXCHANGE = "topic-exchange";
    public static final String FANOUT_EXCHANGE = "fanout-exchange";
    public static final String HEADER_QUEUE = "header-queue";
    public static final String HEADER_EXCHANGE = "header-exchange";

    //direct 方式
    @Bean
    public static Queue queue(){
        return new Queue(MQConfig.QUEUE,true);
    }

    //topic 方式  queue exchange binding
    @Bean
    public Queue topic_queue1(){
        return new Queue(MQConfig.TOPIC_QUEUE1,true);
    }
    @Bean
    public Queue topic_queue2(){
        return new Queue(MQConfig.TOPIC_QUEUE2,true);
    }
    @Bean
    public TopicExchange topic_exchange(){
        return new TopicExchange(MQConfig.TOPIC_EXCHANGE);
    }
    @Bean
    public Binding topicBinding1(){
        return BindingBuilder.bind(topic_queue1()).to(topic_exchange()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2(){
        return BindingBuilder.bind(topic_queue2()).to(topic_exchange()).with("topic.#");
    }

    //fanout 广播
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(MQConfig.FANOUT_EXCHANGE);
    }
    @Bean
    public Binding fanoutBinding1(){
        return BindingBuilder.bind(topic_queue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2(){
        return BindingBuilder.bind(topic_queue2()).to(fanoutExchange());
    }

    //header 模式
    @Bean
    public Queue headerQueue(){
        return new Queue(MQConfig.HEADER_QUEUE,true);
    }
    @Bean
    public HeadersExchange headerExchange(){
        return new HeadersExchange(MQConfig.HEADER_QUEUE);
    }
    @Bean
    public Binding headerBinding(){
        Map<String,Object> header = new HashMap<String, Object>();
        header.put("head1","key1");
        header.put("head2","key2");

        return BindingBuilder.bind(headerQueue()).to(headerExchange()).whereAll(header).match();
    }

}
