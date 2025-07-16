package hapum.hapum.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import hapum.hapum.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;


@Component
public class AdminInterceptor implements HandlerInterceptor {

    

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
    	HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute("loginMember");

		
        // 예: 세션에 저장된 객체가 User 클래스라고 가정
        if (user != null && user instanceof User) {
//            User user = (User) sessionObj;
            // 관리자 판단 로직: username 또는 role 기준
            if ("admin".equals(user.getEmail())) {
                return true; // 관리자면 통과
            }
        }

        // 접근 거부
        response.sendRedirect("/error/access-denied");
        return false;
    }
}

