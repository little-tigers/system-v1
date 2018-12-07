package cn.v1.rocketmq.core.impl;

import cn.v1.rocketmq.core.RocketMQProducer;
import cn.v1.rocketmq.service.RocketMQService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: wr
 * @Date: 2018/11/6
 * @Description:
 */
@Service
public class RocketMQServiceImpl implements RocketMQService {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQServiceImpl.class);

    @Autowired
    private RocketMQProducer rocketMQProducer;


    @Override
    public void send(Message message) {
        try {
            SendResult sendResult = rocketMQProducer.getMQProducer().send(message);
            // 当消息发送失败时如何处理
            if (sendResult == null || sendResult.getSendStatus() != SendStatus.SEND_OK) {
                logger.error("消息发送失败！！" + rocketMQProducer.getInstanceName());
            }
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
