package mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtils {
    private final static String COOKIE_DOMAIN=".yiku.com";
    private final static String COOKIE_NAME="LOGIN_INFO";

    public static String readCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie:cookies){
            log.info("cookiename:{},cookievalue:{}",cookie.getName(),cookie.getValue());
            if (StringUtils.equals(COOKIE_NAME,cookie.getName())){
                String value = cookie.getValue();
                log.info("cookiename:{},cookievalue:{}hahaha",cookie.getName(),cookie.getValue());
                return value;
            }
        }
        return null;
    }
    public static void writeLoginToken(HttpServletResponse response,String token){
        Cookie ck = new Cookie(COOKIE_NAME,token);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");//代表设置在根目录
        ck.setHttpOnly(false);
        //单位是秒。
        //如果这个maxage不设置的话，cookie就不会写入硬盘，而是写在内存。只在当前页面有效。
        ck.setMaxAge(60 * 60 * 24 * 365);//如果是-1，代表永久
        log.info("write cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
        response.addCookie(ck);
    }
    public static void delCookie(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie:cookies){
            if (StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                cookie.setMaxAge(0);
                cookie.setDomain(COOKIE_DOMAIN);
                cookie.setPath("/");//代表设置在根目录
                response.addCookie(cookie);
            }
        }
    }



}
