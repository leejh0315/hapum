package hapum.hapum.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/error")
public class ErrorController {

    @RequestMapping("/400")
    public String error400(HttpServletRequest req, HttpServletResponse resp, Model model) {
        log.info("error 401");
        return "error/400";
    }

    @RequestMapping("/404")
    public String error404(HttpServletRequest req, HttpServletResponse resp,Model model) {
        log.info("error 404");
        return "error/404";
    }
    @RequestMapping("/405")
    public String error405(HttpServletRequest req, HttpServletResponse resp,Model model) {
        log.info("error 405");
        return "error/405";
    }

    @RequestMapping("/500")
    public String error500(HttpServletRequest req, HttpServletResponse resp,Model model) {
        log.info("error 500");
        return "error/500";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/accessDenied"; // 템플릿 파일 이름
    }
    
}

