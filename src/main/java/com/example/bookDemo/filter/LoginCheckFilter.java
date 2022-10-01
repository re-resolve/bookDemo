package com.example.bookDemo.filter;

import com.alibaba.fastjson.JSON;
import com.example.bookDemo.common.BaseContext;
import com.example.bookDemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("filter initializing");
        Filter.super.init(filterConfig);
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        String requestURI = request.getRequestURI();
        
        
        //不需要处理的请求路径(用户登录和登出)
        String[] urls = new String[]{
                "/user/login",
                "/user/logout"
        };
        //判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        
        //如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        //判断登录状态，如果已经登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            //通过session获得用户id
            Long userId = (Long) request.getSession().getAttribute("user");
            
            log.info("用户已登陆，用户id为：{}", userId);
            
            //将用户id设置在 ThreadLocal 中
            BaseContext.setCurrentId(userId);
            
            filterChain.doFilter(request, response);
            return;
        }
        //如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("本次请求{}失败", requestURI);
        response.getWriter().write(JSON.toJSONString(Result.error("NOT LOGIN")));
        return;
    }
    
    /**
     * 服务关闭时，web服务器销毁Filter的实例对象（程序结束时销毁）
     */
    @Override
    public void destroy() {
        log.info("web destroying Filter实例对象...");
        Filter.super.destroy();
    }
    
    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
