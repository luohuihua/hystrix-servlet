package com.luohh.servlet;

import com.luohh.hystrix.ServletHystrixCommand;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.metric.consumer.HealthCountsStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * 简单的统计页面
 *
 * @author luohuihua
 */
public class HystrixRequestStatisticsServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();


        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println(" <meta charset='utf-8'>");
        out.println("<title>请求统计</title>");
        out.println("<meta name='renderer' content='webkit'>");
        out.println("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>");
        out.println("<meta name='viewport'' content='width=device-width, initial-scale=1, maximum-scale=1'>");
        out.println(" <link rel='stylesheet' href='https://www.layuicdn.com/layui/css/layui.css'  media='all'>");
        out.println("</head>");
        out.println("<body>");

        out.println("<table class='layui-hide' id='table'></table>");
        out.println("<script src='https://www.layuicdn.com/layui/layui.js' charset='utf-8'></script>");
        out.println("<script>");
        out.println(" layui.use('table', function(){");
        out.println(" var table = layui.table;");
        out.println(" table.render({");
        out.println(" elem: '#table'");
        out.println(",cols: [[");
        out.println(" {field: 'path', title: '路径', sort: true}");
        out.println(",{field: 'fusing', title: '是否熔断',sort: true}");
        out.println(",{field: 'failureStatistics', title: '失败次数，总次数，失败比率',sort: true}");
        out.println(",{field: 'averageRequestExecutionTime', title: '平均请求执行时间',sort: true}");
        out.println("]]");
        out.println(",data: [");

        Collection<HystrixCommandMetrics> hystrixCommandMetricsList = HystrixCommandMetrics.getInstances();
        if (hystrixCommandMetricsList != null) {
            for (HystrixCommandMetrics hystrixCommandMetrics : hystrixCommandMetricsList) {
                HealthCountsStream healthCountsStream = hystrixCommandMetrics.getHealthCountsStream();
                HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(hystrixCommandMetrics.getCommandGroup()).andCommandKey(hystrixCommandMetrics.getCommandKey()).andThreadPoolKey(hystrixCommandMetrics.getThreadPoolKey());
                ServletHystrixCommand myHystrixCommand = new ServletHystrixCommand(setter);
                out.println("{path:'" + hystrixCommandMetrics.getCommandKey().toString().replace("HystrixCommand-", "") + "'" +
                        ",fusing:'" + (myHystrixCommand.isCircuitBreakerOpen() ? "是" : "否") + "'" +
                        ",failureStatistics:'" + healthCountsStream.getLatest().toString().replace("HealthCounts", "") + "'" +
                        ",averageRequestExecutionTime:'" + hystrixCommandMetrics.getTotalTimeMean() + "'},");
            }
        }
        out.println(" ]");
        out.println(" ,even: true");
        out.println("});");
        out.println("    });");
        out.println("</script>");
        out.println("</body>");
        out.println("</html>");
    }
}
