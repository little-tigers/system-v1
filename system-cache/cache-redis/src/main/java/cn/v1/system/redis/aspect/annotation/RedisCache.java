package cn.v1.system.redis.aspect.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: wr
 * Date: 2018/4/11
 *
 * @usage 缓存注解类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RedisCache {
    String category() default "happygo_dubbo";

    StorageType storage() default StorageType.ENTITY; //指定存储类型

    Class entityClass();//缓存类

    String fieldKey() default ""; //缓存key

    int expire() default 24 * 60 * 60;      //缓存多少秒,默认时间 1天
}
