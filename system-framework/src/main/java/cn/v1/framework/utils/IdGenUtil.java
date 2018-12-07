package cn.v1.framework.utils;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * @Auther: wr
 * @Date: 2018/11/5
 * @Description:
 */
public class IdGenUtil implements Serializable {

    private static final long serialVersionUID = 3826653196172023406L;

    private static SecureRandom random = new SecureRandom();

    public IdGenUtil() {
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static long randomLong() {
        return Math.abs(random.nextLong());
    }

    public static String randomBase62(int length) {
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return EncodesUtil.encodeBase62(randomBytes);
    }

    public static String random(int length) {
        return String.valueOf(random.nextDouble()).substring(2, 2 + length);
    }

    public static void main(String[] args) {
        System.out.println(uuid());
        System.out.println(uuid().length());
        System.out.println(random(4));
    }
}
