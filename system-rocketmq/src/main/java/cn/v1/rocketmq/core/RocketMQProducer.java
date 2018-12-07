package cn.v1.rocketmq.core;

import cn.v1.rocketmq.util.RunTimeUtil;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: wr
 * @Date: 2018/11/6
 * @Description: 生产者
 */
public class RocketMQProducer {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQProducer.class);

    private MQProducer mqProducer;

    private String producerGroup;

    private String namesrvAddr;

    private String instanceName;

    private int retryTimes;

    public void init() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer(producerGroup);
        instanceName = this.getInstanceName();
        producer.setNamesrvAddr(this.namesrvAddr);
        producer.setInstanceName(instanceName);
        producer.setRetryTimesWhenSendFailed(this.retryTimes);
        producer.setTransactionCheckListener(transactionCheckListener
                        -> LocalTransactionState.COMMIT_MESSAGE);
        Runtime.getRuntime().addShutdownHook(new Thread(()-> producer.shutdown()));
   /*     Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                producer.shutdown();
            }
        }));*/
        producer.start();
        this.mqProducer = producer;
        logger.info("rocketMQ初始化生产者完成[producerGroup：" + producerGroup + "，instanceName："+ instanceName +"]");
    }

    public void destroy() {
        mqProducer.shutdown();
        logger.info("rocketMQ生产者[producerGroup: " + producerGroup + ",instanceName: "+ instanceName +"]已停止");
    }

    public MQProducer getMQProducer() {
        return mqProducer;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public String getInstanceName() {
        return RunTimeUtil.getRocketMqUniqueInstanceName();
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

}
