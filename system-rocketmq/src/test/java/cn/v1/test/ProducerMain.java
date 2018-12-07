package cn.v1.test;

import cn.v1.rocketmq.core.RocketMQProducer;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Intellij IDEA
 * User: wr
 * Date: 2018/5/31
 */
public class ProducerMain {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/spring-*.xml");
        RocketMQProducer rocketMqProducer = context.getBean(RocketMQProducer.class);
        String messageStr = "你好，中国!";
        byte[] body =  JSON.toJSONString(messageStr).getBytes();
        Message msg = new Message("MyTopic", "MyTag", body);
        SendResult sendResult = null;
        try {
            sendResult =  rocketMqProducer.getMQProducer().send(msg);
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 当消息发送失败时如何处理
        if (sendResult == null || sendResult.getSendStatus() != SendStatus.SEND_OK) {
            // TODO
            System.out.printf("send mq is ok!");
        }
    }
}
