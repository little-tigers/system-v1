package cn.v1.system.redis.aspect.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: wr
 * Date: 2018/4/11
 * @usage 清除过期缓存注解，放置于update delete insert 类型逻辑之上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RedisEvict {
    String category() default "happygo_dubbo";

    Class entityClass();//缓存类

    String fieldKey() default ""; //缓存key
}
