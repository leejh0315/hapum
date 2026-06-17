package hapum.hapum.controller.user;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hapum.hapum.domain.News;
import hapum.hapum.domain.Notification;
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramAdd;
import hapum.hapum.domain.User;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.OrganizationService;
import hapum.hapum.service.ProgramService;
import hapum.hapum.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DefatultController {
	private final NewsService newsService;
	private final ProgramService programService;
	private final NotificationService notificationService;
	private final VisitorService visitorService;
	private final OrganizationService organizationService;
	@GetMapping("/")
	public String getMain(HttpServletRequest req, Model model) {
	    // 방문 기록 남기기
	    visitorService.logVisit(req);

	    addUserToModel(req, model);

	    List<Program> programs = programService.selectHomeProgram();
	    List<Program> popupProgram = programService.selectPopupProgram();
	    List<News> news = newsService.select20news();
	    List<Notification> notifications = notificationService.selectAll();

	    model.addAttribute("popupProgram", popupProgram);
	    model.addAttribute("news", news);
	    model.addAttribute("programs", programs);
	    model.addAttribute("notifications", notifications);

	    return "main/main";
	}

	
	private void addUserToModel(HttpServletRequest req, Model model) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		model.addAttribute("user", user);
		
		// [추가] 이제 모든 페이지의 header 조각이 프로그램 sub-menu 데이터를 참조할 수 있습니다.
				List<ProgramAdd> headerProgramAdds = programService.selectAllProgramAdd();
				model.addAttribute("headerProgramAdds", headerProgramAdds);
				
				List<Organization> organizationList = organizationService.selectAllOrganization();
				model.addAttribute("organizationList", organizationList);
	}
}
