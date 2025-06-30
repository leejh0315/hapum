package hapum.hapum.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import hapum.hapum.domain.User;


@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginMember");
        
        // 세션이 없거나 user 속성이 없으면 로그인 페이지로 리다이렉트
        if (user == null ) {
            response.sendRedirect("/auth/login");
            return false;
        }

        return true; // 세션 유효하면 요청 진행
    }
}