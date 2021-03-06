package com.finals.handy.shiro.filter;

import com.finals.handy.constant.ResponseCode;
import com.finals.handy.shiro.JwtToken;
import com.finals.handy.shiro.exception.MyTokenErrorException;
import com.finals.handy.shiro.exception.MyTokenExpireException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author zsw
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            if(executeLogin(request, response)){
                return true;
            }else{
                return false;
            }
        } else {
            //如果请求没有携带token请求头
            request.setAttribute("status",ResponseCode.NOT_LOGIN.getValue());
            return false;
        }
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        Integer code = (Integer) request.getAttribute("status");
//        Map<String,Object> map = new HashMap<>(16);
//        map.put("code",code);
//        String json = JSON.toJSONString(map);
        HttpServletResponse response1 = (HttpServletResponse) response;
        PrintWriter writer = response1.getWriter();
        writer.print(code);
        return false;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getParameter("accessToken");
        return token != null;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getParameter("accessToken");
        JwtToken jwtToken = new JwtToken(token);
        try {
            getSubject(request, response).login(jwtToken);
            return true;
        } catch (MyTokenExpireException e) {
            httpServletRequest.setAttribute("status", ResponseCode.TOKEN_EXPIRE.getValue());
            return false;
        } catch (MyTokenErrorException e) {
            httpServletRequest.setAttribute("status", ResponseCode.TOKEN_ERROR.getValue());
            return false;
        }

        //提交给realm进行登入，如果错误的话会抛出异常
    }


}
