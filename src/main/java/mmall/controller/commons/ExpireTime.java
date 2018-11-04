package mmall.controller.commons;

import mmall.pojo.User;
import mmall.util.CookieUtils;
import mmall.util.JsonUtil;
import mmall.util.RedisPoolUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ExpireTime implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest=(HttpServletRequest)servletRequest;
        String token= CookieUtils.readCookie(httpServletRequest);
        if (!(StringUtils.isEmpty(token))){
            User user= JsonUtil.string2Obj(token,User.class);
            RedisPoolUtils.expireExTime(token,60*60*30);
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
