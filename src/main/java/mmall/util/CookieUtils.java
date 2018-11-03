package mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtils {
    private final static String COOKIE_DOMAIN="www.yiku.com";
    private final static String COOKIE_NAME="LOGIN_INFO";

    public static String readCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie:cookies){
            log.info("cookiename:{},cookievalue:{}",cookie.getName(),cookie.getValue());
            if (StringUtils.equals(COOKIE_NAME,cookie.getName())){
                String value = cookie.getValue();
                log.info("cookiename:{},cookievalue:{}",cookie.getName(),cookie.getValue());
                return value;
            }
        }
        return null;
    }
    public static void writeCookie(HttpServletResponse response, String token){
        Cookie ck=new Cookie(COOKIE_NAME,token);
        ck.setHttpOnly(true);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");
        ck.setMaxAge(60*60*24*365);
        log.info("cookie_domain:{},cookie_name:{},token:{}",ck.getDomain(),ck.getName(),token);
        response.addCookie(ck);
    }
    public static void delCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie:cookies){
            if (StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                cookie.setDomain(COOKIE_DOMAIN);
                cookie.setMaxAge(0);
            }
        }
    }



}
