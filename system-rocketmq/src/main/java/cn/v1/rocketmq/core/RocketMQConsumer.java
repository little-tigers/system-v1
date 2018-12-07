package cn.v1.rocketmq.core;

import cn.v1.rocketmq.factory.AbstractRocketMQConsumer;
import cn.v1.rocketmq.listener.MessageListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: wr
 * @Date: 2018/11/6
 * @Description: 消费者
 */
public class RocketMQConsumer extends AbstractRocketMQConsumer {

    @Override
    public String getTopic() {
        return "MyTopic";
    }

    @Override
    public String getTag() {
        return "MyTog";
    }

    @Override
    public MessageListenerConcurrently getMessageListener() {
        return new MessageListener();
    }
}
