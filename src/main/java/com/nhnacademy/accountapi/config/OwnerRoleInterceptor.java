package com.nhnacademy.accountapi.config;

import com.nhnacademy.accountapi.entity.UserRole;
import com.nhnacademy.accountapi.exception.UserNotAllowException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 오너 권한 확인 인터셉터
@Component
public class OwnerRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        // 헤더에서 X-USER-ROLE 추출
        String role = request.getHeader("X-USER-ROLE");

        // 추출한 role이 OWNER인지 확인
        if (role==null || !UserRole.OWNER.name().equals(role)) {
            throw new UserNotAllowException("오너(Owner) 권한이 필요합니다.");
        }

        // 확인후 true 반환
        return true;
    }
}
