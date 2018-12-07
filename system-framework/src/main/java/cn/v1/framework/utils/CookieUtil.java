package cn.v1.framework.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @Auther: wr
 * @Date: 2018/12/3
 * @Description:
 */
public class CookieUtil {

    public static void setCookie(HttpServletResponse response, String name, String value) {
        setCookie(response, name, value, 86400);
    }

    public static void setCookie(HttpServletResponse response, String name, String value, String path) {
        setCookie(response, name, value, path, 86400);
    }

    public static void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        setCookie(response, name, value, "/", maxAge);
    }


    public static void setCookie(HttpServletResponse response, String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, (String)null);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);

        try {
            cookie.setValue(URLEncoder.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        response.addCookie(cookie);
    }

    public static String getCookie(HttpServletRequest request, String name) {
        return getCookie(request, (HttpServletResponse)null, name, false);
    }

    public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        return getCookie(request, response, name, true);
    }

    public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name, boolean isRemove) {
        String value = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Cookie[] arr = cookies;
            for(int i = 0; i < cookies.length; ++i) {
                Cookie cookie = arr[i];
                if (cookie.getName().equals(name)) {
                    try {
                        value = URLDecoder.decode(cookie.getValue(), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (isRemove) {
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }
        }
        return value;
    }
}
