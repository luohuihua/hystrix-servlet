package com.luohh.servlet.filter;

import com.luohh.hystrix.ServletHystrixCommand;
import com.netflix.hystrix.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

/**
 * 熔断过滤器
 *
 * @author luohuihua
 */
public class HystrixRequestServletFilter implements Filter {
    /**
     * 读取配置
     */
    private Properties prop = new Properties();
    /**
     * 是否读取完成
     */
    private boolean initEnd = false;

    /**
     * 初始化一些配置
     *
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        InputStream in = null;
        try {
            in = HystrixRequestServletFilter.class.getClassLoader().getResourceAsStream("hystrix-config.properties");
            prop.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!initEnd) {
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    System.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
//            System.setProperties(prop);
            initEnd = true;
        }
        String requestUrl = ((HttpServletRequest) servletRequest).getRequestURI();
        StringBuffer key = new StringBuffer(requestUrl);
//        String queryString = ((HttpServletRequest) servletRequest).getQueryString();
//        if (queryString != null && queryString.length() > 0) {
//            key.append("?").append(queryString);
//        }
        HystrixCommandGroupKey commandGroupKey = HystrixCommandGroupKey.Factory.asKey("ServletRequestGroup");
        HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey("HystrixCommand-" + key.toString());
        HystrixThreadPoolKey threadPoolKey = HystrixThreadPoolKey.Factory.asKey("HystrixThreadPool-" + key.toString());

        //设置参数
        HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(commandGroupKey).andCommandKey(commandKey).andThreadPoolKey(threadPoolKey);
        ServletHystrixCommand myHystrixCommand = new ServletHystrixCommand(servletRequest, servletResponse, filterChain, setter);
        //同步执行
        String message = myHystrixCommand.execute();
        if (message != null && message.length() > 0) {
            System.out.println(message);
            PrintWriter writer = null;
            try {
                writer = servletResponse.getWriter();
                writer.write("{\n" +
                        "\t\"statusCode\": \"999\",\n" +
                        "\t\"message\": \"" + message + "\"\n" +
                        "}");
                writer.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    @Override
    public void destroy() {

    }
}
