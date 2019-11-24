package com.spacex.rule.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spacex.rule.common.ErrorCodeEnum;
import com.spacex.rule.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.spacex.rule.util.JsonResult;

/**
 * Token 验证
 */
@Configuration
public class LoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest reqs = (HttpServletRequest) request;
        res.setHeader("Access-Control-Allow-Origin","*");
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Methods", "PUT,DELETE,POST,GET,OPTIONS");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Access-Control-Allow-Headers", "authorization,Content-Type,Accept");

        if (!(request instanceof HttpServletRequest)) {
            doErrorResponse((HttpServletResponse) response, "非法请求");
            return;
        }

//        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        chain.doFilter(request, response);
    }

    private void doErrorResponse(HttpServletResponse response, String errorMsg) {
        String output = JsonUtils.object2Json(JsonResult.fail(ErrorCodeEnum.TOKEN_ERROR.getCode(), errorMsg));
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(output);
        } catch (IOException ex) {
            log.error("token failed!", ex);
        }
    }


    @Override
    public void destroy() {
    }
}
