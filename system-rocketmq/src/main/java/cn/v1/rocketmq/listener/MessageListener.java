package cn.v1.rocketmq.listener;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Intellij IDEA
 * User: wr
 * Date: 2018/5/31
 */
public class MessageListener implements MessageListenerConcurrently {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MessageListener.class);


    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for (MessageExt messageExt : list) {
            logger.info( new String(messageExt.getBody()));
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
