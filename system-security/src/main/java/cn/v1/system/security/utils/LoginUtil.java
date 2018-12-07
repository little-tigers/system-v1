package cn.v1.system.security.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by wr on 2017/12/27.
 */
public class LoginUtil {

    /**
     * 是否是验证码登录
     * @param userName 用户名
     * @param isFail 计数加1
     * @param clean 计数清零
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean isValidateCodeLogin(String userName, boolean isFail, boolean clean){
        Map<String, Integer> loginFailMap = (Map<String, Integer>)CacheUtils.get("loginFailMap");
        if (loginFailMap==null){
            loginFailMap = Maps.newHashMap();
            CacheUtils.put("loginFailMap", loginFailMap);
        }
        Integer loginFailNum = loginFailMap.get(userName);
        if (loginFailNum==null){
            loginFailNum = 0;
        }
        if (isFail){
            loginFailNum++;
            loginFailMap.put(userName, loginFailNum);
        }
        if (clean){
            loginFailMap.remove(userName);
        }
        return loginFailNum >= 3;
    }
}
