package hapum.hapum.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import hapum.hapum.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginMember");

        String ipAddress = request.getRemoteAddr();
		String queryString = request.getQueryString();
	    String method = request.getMethod();              // HTTP 메서드(GET, POST 등)
	    String requestUri = request.getRequestURI();      // 요청 URL 경로 (/admin/dashboard 등)
	    String currentTime = java.time.LocalDateTime.now().toString();  // 현재 시간 (ISO 8601 포맷)
	    String userInfo = (user != null) 
	    	    ? user.getId() + " : " + user.getName() 
	    	    : "guest";
	    
		log.info("User '{}' from IP [{}] accessed [{}] {}{} at {}", 
			userInfo, 
		    ipAddress, 
		    method, 
		    requestUri, 
		    (queryString != null ? "?" + queryString : ""), 
		    currentTime);
		
        
        
        // 세션이 없거나 user 속성이 없으면 로그인 페이지로 리다이렉트
        if (user == null ) {
            response.sendRedirect("/auth/login");
            return false;
        }

        return true; // 세션 유효하면 요청 진행
    }
}