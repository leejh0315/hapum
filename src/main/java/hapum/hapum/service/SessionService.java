package hapum.hapum.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class SessionService {
	
	public static final String SESSION_COOKIE_NAME = "tempSessionId";
	private Map<String, Object> sessionMap = new HashMap<String, Object>();

	public void create(Object object, HttpServletResponse resp) {
		String sessionId = UUID.randomUUID().toString();
		sessionMap.put(sessionId, object);

		Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
		resp.addCookie(cookie);

	}

	public void remove(HttpServletRequest req) {
		Cookie cookie = findCookie(req, SESSION_COOKIE_NAME);
		if(cookie != null) {
			sessionMap.remove(cookie.getValue());
		}
	}

	public Object getSessionObject(HttpServletRequest req) {
		Cookie cookie = findCookie(req, SESSION_COOKIE_NAME);
		if(cookie != null) {
			return sessionMap.get(cookie.getValue());
		}
		return null;
	}

	public Cookie findCookie(HttpServletRequest req, String cookieName) {
		if(req.getCookies() != null) {
			for(Cookie cookie : req.getCookies()) {
				if(cookie.getName().equals(cookieName)) {
					return cookie;
				}
			}
		}
		return null;
	}
}
