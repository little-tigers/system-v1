package cn.v1.rocketmq.factory;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: wr
 * @Date: 2018/11/6
 * @Description: 生产者
 */
public abstract class AbstractRocketMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRocketMQConsumer.class);

    private DefaultMQPushConsumer mqPushConsumer;

    private String producerGroup;

    private String namesrvAddr;

    public abstract String getTopic();

    public abstract String getTag();

    public abstract MessageListenerConcurrently getMessageListener();


    public void init() throws MQClientException {
        mqPushConsumer = new DefaultMQPushConsumer(producerGroup);
        mqPushConsumer.setNamesrvAddr(this.namesrvAddr);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        mqPushConsumer.subscribe(getTopic(), "*");
        mqPushConsumer.registerMessageListener(getMessageListener());
        mqPushConsumer.start();
        logger.debug("rocketMQ客户端初始化生产者完成[producerGroup：" + producerGroup);
    }

    public void destroy() {
        mqPushConsumer.shutdown();
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }
}
