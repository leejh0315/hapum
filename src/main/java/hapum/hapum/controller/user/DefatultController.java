package hapum.hapum.controller.user;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hapum.hapum.domain.News;
import hapum.hapum.domain.Notification;
import hapum.hapum.domain.Program;
import hapum.hapum.domain.User;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.ProgramService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DefatultController {
	private final NewsService newsService;
	private final ProgramService programService;
	private final NotificationService notificationService;

	
	@GetMapping("/")
	public String getMain(HttpServletRequest req, Model model) {
		addUserToModel(req, model);
//		List<ProgramAdd> programs = programService.selectAllProgramsMain();

		List<Program> programs = programService.selectHomeProgram();
		
		List<Program> popupProgram = programService.selectPopupProgram();
		
		List<News> news = newsService.select20news();
		
		
		List<Notification> notifications = notificationService.selectAll();

		
		model.addAttribute("popupProgram", popupProgram);

		model.addAttribute("news",news);
		model.addAttribute("programs", programs);
		model.addAttribute("notifications", notifications);
		return "main/main";
	}
	
	private void addUserToModel(HttpServletRequest req, Model model) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		model.addAttribute("user", user);
		System.out.println("This user : " + user);
	}
}
