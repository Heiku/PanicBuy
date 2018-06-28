package com.heiku.panicbuy.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MessageQueueConfig {

    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADERS_QUEUE = "headers.queue";
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    public static final String FANOUT_EXCHANGE = "fanout.exchange";
    public static final String HEADERS_EXCHANGE = "headers.exchange";

    public static final String SECKILL_QUEUE = "seckill.queue";


    /**
     * 返回 seckill消息队列
     *
     * @return
     */
    @Bean
    public Queue queue(){
        return new Queue(SECKILL_QUEUE, true);
    }

    /*// direct
    @Bean
    public Queue queue(){
        return new Queue(QUEUE, true);
    }

    // topic
    @Bean
    public Queue topicQueue1(){
        return new Queue(TOPIC_QUEUE1, true);
    }

    @Bean
    public Queue topicQueue2(){
        return new Queue(TOPIC_QUEUE2, true);
    }


    *//**
     * topic 模式
     *
     * @return
     *//*
    // topic exchanger
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    // topic binding
    @Bean
    public Binding topicBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }

    @Bean
    public Binding topicBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
    }


    *//**
     * fanout 模式
     *
     * @return
     *//*
    // fanout exchanger
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(MessageQueueConfig.FANOUT_EXCHANGE);
    }

    // fanout binding
    @Bean
    public Binding fanoutBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }


    *//**
     * headers 模式
     *
     * @return
     *//*
    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(MessageQueueConfig.HEADERS_EXCHANGE);
    }

    @Bean
    public Queue headersQueue(){
        return new Queue(MessageQueueConfig.HEADERS_QUEUE, true);
    }

    @Bean
    public Binding headersBinding(){
        Map<String, Object> map = new HashMap<>();
        map.put("header1", "value1");
        map.put("header2", "value2");

        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(map).match();
    }*/
}
