package cn.v1.test;

import cn.v1.rocketmq.core.RocketMQConsumer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Intellij IDEA
 * User: wr
 * Date: 2018/5/31
 */
public class ConsumerMain {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/spring-*.xml");
        RocketMQConsumer rocketMqConsumer = context.getBean(RocketMQConsumer.class);
    }
}
