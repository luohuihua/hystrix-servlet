package com.luohh.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author luohuihua
 */
public class ServletHystrixCommand extends HystrixCommand<String> {
    /**
     * ServletRequest
     */
    private ServletRequest servletRequest;
    /**
     * ServletResponse
     */
    private ServletResponse servletResponse;
    /**
     * FilterChain
     */
    private FilterChain filterChain;

    /**
     * 构造方法
     *
     * @param group
     */
    public ServletHystrixCommand(HystrixCommandGroupKey group) {
        super(group);
    }

    public ServletHystrixCommand(HystrixCommandGroupKey group, HystrixThreadPoolKey threadPool) {
        super(group, threadPool);
    }

    public ServletHystrixCommand(HystrixCommandGroupKey group, int executionIsolationThreadTimeoutInMilliseconds) {
        super(group, executionIsolationThreadTimeoutInMilliseconds);
    }

    public ServletHystrixCommand(HystrixCommandGroupKey group, HystrixThreadPoolKey threadPool, int executionIsolationThreadTimeoutInMilliseconds) {
        super(group, threadPool, executionIsolationThreadTimeoutInMilliseconds);
    }

    public ServletHystrixCommand(Setter setter) {
        super(setter);
    }

    /**
     * 成功进入访问
     *
     * @return
     * @throws Exception
     */
    @Override
    protected String run() throws Exception {
        filterChain.doFilter(servletRequest, servletResponse);
        return "";
    }

    /**
     * 熔断禁止访问
     *
     * @return
     */
    @Override
    protected String getFallback() {
        return "Fuse triggered, access failed";
    }

    public ServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public ServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(ServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }
}
