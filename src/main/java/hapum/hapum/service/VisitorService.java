package hapum.hapum.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import hapum.hapum.domain.VisitorLog;
import hapum.hapum.mapper.VisitorMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class VisitorService {
    private final VisitorMapper visitorMapper;

    public VisitorService(VisitorMapper visitorMapper) {
        this.visitorMapper = visitorMapper;
    }

    public void logVisit(HttpServletRequest request) {
        HttpSession session = request.getSession();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String visitKey = "VISITED_" + today;


        if (session.getAttribute(visitKey) == null) {
            VisitorLog log = new VisitorLog();
            log.setSessionId(session.getId());
            log.setVisitTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

            visitorMapper.insertVisitorLog(log);
            session.setAttribute(visitKey, true);
        }
    }

    public int getDailyVisitCount(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return visitorMapper.countByDateTimeRange(start, end);
    }
    public int getTotalVisitCount() {
        return visitorMapper.countAll();
    }
    
}
	
