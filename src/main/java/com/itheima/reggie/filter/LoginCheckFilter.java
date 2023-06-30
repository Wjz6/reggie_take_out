package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*") // url确定拦截哪些请求路径
@Slf4j

public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    //为什么要加这个？因为数组里是通配符，如果传入的是index.html，无法直接匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;  //向下转型
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();  // /backend/index.html

        log.info("拦截到请求: {}", requestURI);

        //定义不需要处理的请求路径
        //login.js内
        String[] urls=new String[]{
          "/employee/login",
          "/employee/logout",
          "/backend/**",
          "/front/**",
          "/common/**",
          "/user/sendMsg",
          "/user/login"
        };

        //2.判断本次请求是否需要处理
        boolean check = check(urls,requestURI);

        //3.如果不需要处理，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request, response);  //拦截传入请求和传出响应
            return;
        }

        //4-1.判断服务端登陆状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录,用户id为:{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrent(empId);

            filterChain.doFilter(request, response);  //拦截传入请求和传出响应
            return;
        }

        //4-2.判断移动端登陆状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已登录,用户id为:{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrent(userId);

            filterChain.doFilter(request, response);  //拦截传入请求和传出响应
            return;
        }

        log.info("用户未登录");
        //5.如果未登录则返回未登录结果,通过输出流方式向客户端页面响应数据
        //具体可见backend/js/request.js，有个响应拦截器，规定了什么情况下页面实现跳转
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));  //R对象转JSON，通过输出流写回去
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if(match)
                return true;
        }
        return false;
    }
}
