package cn.v1.rocketmq.service;

import org.apache.rocketmq.common.message.Message;

/**
 * @Auther: wr
 * @Date: 2018/11/6
 * @Description:
 */
public interface RocketMQService {

    void send(Message message);

}
