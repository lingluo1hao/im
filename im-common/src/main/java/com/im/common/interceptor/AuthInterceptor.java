package com.im.common.interceptor;

import com.im.common.context.UserContext;
import com.im.common.dto.UserInfoDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 🌟 检查网关是否颁发了白名单通行证
        String isWhite = request.getHeader("X-Is-White");
        if ("true".equals(isWhite)) {
            return true; // 网关认证的公开接口（如登录、注册），直接无条件放行
        }

        // 2. 非白名单接口，必须接受 X-User-Id 校验
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            // 此时被拦截的，绝对是绕过网关、或者网关校验失败的非法内网直连请求
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":415,\"msg\":\"内网非授权访问被拒绝\"}");
            return false;
        }

        // 3. 正常解析并装载上下文
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(Long.parseLong(userIdStr));
        UserContext.set(userInfoDTO);
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 4. 极致严谨清理：防 Tomcat 线程池污染
        UserContext.clear();
    }
}
