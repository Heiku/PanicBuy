package com.heiku.panicbuy.rabbitmq;


import com.heiku.panicbuy.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {

    private static Logger log = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;


    /**
     * 发送 seckillmessage
     *
     * @param message
     */
    public void sendSeckillMessage(SeckillMessage message) {
        String msg = RedisService.beanToStr(message);
        log.info("send seckill message : " + msg);

        amqpTemplate.convertAndSend(MessageQueueConfig.SECKILL_QUEUE, msg);
    }


    /*public void send(Object message){
        String msg = RedisService.beanToStr(message);

        log.info("send message : " + msg);
        amqpTemplate.convertAndSend(MessageQueueConfig.QUEUE, msg);
    }


    //topic
    public void sendTopic(Object message){
        String msg = RedisService.beanToStr(message);

        log.info("send message : " + msg);
        amqpTemplate.convertAndSend(MessageQueueConfig.TOPIC_EXCHANGE, "topic.key1", msg + "1");
        amqpTemplate.convertAndSend(MessageQueueConfig.TOPIC_EXCHANGE, "topic.key2", msg + "2");
    }


    //fanout
    public void sendFanout(Object message){
        String msg = RedisService.beanToStr(message);

        log.info("send message : " + msg);
        amqpTemplate.convertAndSend(MessageQueueConfig.FANOUT_EXCHANGE, "",msg );
    }


    //headers
    public void sendHeaders(Object message){
        String msg = RedisService.beanToStr(message);

        log.info("send message : " + msg);

        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");

        Message obj = new Message(msg.getBytes(), properties);

        amqpTemplate.convertAndSend(MessageQueueConfig.HEADERS_EXCHANGE, "", obj);
    }*/


}
