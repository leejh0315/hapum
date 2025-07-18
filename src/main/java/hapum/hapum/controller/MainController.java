package hapum.hapum.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hapum.hapum.domain.BlockedDay;
import hapum.hapum.domain.FixedReservation;
import hapum.hapum.domain.News;
import hapum.hapum.domain.Notification;
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationPost;
import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramAdd;
import hapum.hapum.domain.ProgramSub;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.User;
import hapum.hapum.service.ConvertService;
import hapum.hapum.service.EmailService;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.OrganizationService;
import hapum.hapum.service.ProgramService;
import hapum.hapum.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

	private final NewsService newsService;
	private final ProgramService programService;
	private final EmailService emailService;
	private final ReservationService reservationService;
	private final OrganizationService organizationService;
	private final NotificationService notificationService;

	private final ConvertService convertService;
	
	@Value("${file.upload-dir}")
	private String uploadDir; // e.g. "./uploads"

	@GetMapping("/main")
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

	@GetMapping("/introduce")
	public String getIntroduce(HttpServletRequest req, Model model) {
		addUserToModel(req, model);
		return "main/introduce";
	}

	@GetMapping("/information")
	public String getInformation(HttpServletRequest req, Model model) {
		addUserToModel(req, model);
		return "main/information";
	}

	@GetMapping("/program")
	public String getProgram(HttpServletRequest req, Model model) {
		addUserToModel(req, model);
		List<ProgramAdd> programAdd = programService.selectAllProgramAdd();
		model.addAttribute("programs", programAdd);
		return "main/program";
	}

	@GetMapping("/programList/{id}")
	public String getProgramList(HttpServletRequest req, Model model, @PathVariable("id") Long id) {
		addUserToModel(req, model);
		List<Program> programs = programService.selectProgramByAddId(id);
		List<ProgramAdd> programAdds = programService.selectAllProgramAdd();

		Optional<ProgramAdd> result = programAdds.stream().filter(p -> p.getId() == id).findFirst();

		ProgramAdd pa = new ProgramAdd();
		;

		if (result.isPresent()) {
			pa = result.get();
			// 사용
		}

		model.addAttribute("pa", pa);
		model.addAttribute("programs", programs);
		model.addAttribute("programAdds", programAdds);
		model.addAttribute("activeTab", id);
		return "main/programList";
	}

	@GetMapping("/program/detail/{id}")
	public String getProgramDetail(@PathVariable("id") Long id, HttpServletRequest req, Model model) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");

		Program program = programService.selectProgramById(id);

		int applyCount = programService.getApplyCount(program.getId()); // 현재 신청자 수

		int remainingSeats = program.getCapacity() - applyCount;
		List<ProgramSub> ps = programService.selectPrograSubmById(id);

		String flag = "N";

		if (user != null) {
			if (ps.stream().anyMatch(sub -> sub.getUserId() == user.getId())) {
				flag = "Y";
			}
		}

		model.addAttribute("user", user);
		model.addAttribute("program", program);
		model.addAttribute("applyCount", applyCount);
		model.addAttribute("remainingSeats", remainingSeats);
		model.addAttribute("flag", flag);
		model.addAttribute("isFull", remainingSeats <= 0); // true면 마감
		return "main/programDetail";
	}

	@PostMapping("/program/subs/{id}")
	@ResponseBody
	public int postProgramSubs(@RequestBody Program program, HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");

		int applyCount = programService.getApplyCount(program.getId());


		ProgramSub ps = new ProgramSub();
		ps.setUserId(user.getId());
		ps.setProgramId(program.getId());

		ps.setOrgName(program.getNeedOrgName());

		
		
		if (!program.getNeedPartCount().equals("N")) {
			ps.setPartCount(Integer.parseInt(program.getNeedPartCount()));

			if (program.getCapacity() - applyCount < Integer.parseInt(program.getNeedPartCount())) {
				return 2;
			}
		}

		ps.setRelation(program.getNeedRelation());

		int temp = programService.programSub(ps);

		
		  log.info("User '{}' (ID: {}) subscribed to program {} (Org: '{}', Relation: '{}', PartCount: {}, Result: {})",
		             user.getName(),
		             user.getId(),
		             program.getId(),
		             ps.getOrgName(),
		             ps.getRelation(),
		             Integer.toString(ps.getPartCount())
		              == null ? "N/A" : ps.getPartCount(),
		             temp);

		
		emailService.sendProgramMessage(user.getEmail(), program);
		
		String result = convertService.generateProgramWordFromTemplate(ps, program, user);
		
		emailService.sendEmailProgram("hapum7179@gmail.com", result);
		
		return temp;
	}

	@GetMapping("/rental")
	public String getRental(HttpServletRequest req, Model model) {
		addUserToModel(req, model);

		List<BlockedDay> blockedDays = reservationService.getBlockedDays();
		List<FixedReservation> fixedReservations = reservationService.getFixedReservations();
		List<Rental> rentals = reservationService.getRentals();

		List<Program> programs = programService.selectThisMonthProgram();

		model.addAttribute("blockedDays", blockedDays);
		model.addAttribute("fixedReservations", fixedReservations);
		model.addAttribute("rentals", rentals);
		model.addAttribute("programs", programs);
		return "main/rental";
	}

	@PostMapping("/submitRental")
	@ResponseBody
	public String submitRental(@RequestBody Rental rental, HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		
		rental.setUserId(user.getId());
		reservationService.insertRental(rental);
		

	    log.info("User '{}' (ID: {}) submitted a rental request. Rental Info: {}",
	             user.getName(),
	             user.getId(),
	             rental);
		
		emailService.sendRentalMessage(user.getEmail(), rental);
		

		String result = convertService.generateRentalWordFromTemplate(rental, user);
		emailService.sendEmailRental("hapum7179@gmail.com", result);
		//hapum7179@gmail.com
		return "success";
	}

	@GetMapping("/news")
	public String getNews(@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, HttpServletRequest req, Model model) {
		addUserToModel(req, model);
		List<News> newsList = newsService.selectAllNews(page, size);
		int totalNews = newsService.getTotalNews();
		int totalPages = (int) Math.ceil((double) totalNews / size);
		model.addAttribute("newsList", newsList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		return "main/news/news";
	}

	@GetMapping("/news/detail/{id}")
	public String getNewsDetail(@PathVariable("id") Long id, HttpServletRequest req, Model model) {
		addUserToModel(req, model);

		News news = newsService.selectById(id);
		model.addAttribute("news", news);

		return "main/news/newsDetail";
	}

	@GetMapping("/organization")
	public String getOrganizationList(Model model, HttpServletRequest req) {
		addUserToModel(req, model);
		List<Organization> organizationList = organizationService.selectAllOrganization();
		model.addAttribute("organizationList", organizationList);
		return "main/organizationList";
	}

	@GetMapping("/organization/{id}")
	public String getOrganizationDetail(@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "5") int size, Model model, HttpServletRequest req,
			@PathVariable("id") Long id) {
		addUserToModel(req, model);
		List<Organization> organizationList = organizationService.selectAllOrganization();
		Organization org = organizationService.selectOrganizationById(id);
		List<OrganizationPost> organizationPost = organizationService.selectByOrgId(id, page, size);
		int totalOrgPost = organizationService.getTotalOrgPost(id);
		int totalPages = (int) Math.ceil((double) totalOrgPost / size);

		model.addAttribute("organizationList", organizationList);
		model.addAttribute("organizationPost", organizationPost);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("id", id);
		model.addAttribute("org", org);
		return "main/organizationDetail";
	}

	@GetMapping("/organization/{orgId}/post/{id}")
	public String getOrganizationPost(HttpServletRequest req, Model model, @PathVariable("id") Long id,
			@PathVariable("orgId") Long orgId) {
		addUserToModel(req, model);
		OrganizationPost organizationPost = organizationService.selectOrgPostById(id);
		List<Organization> organizationList = organizationService.selectAllOrganization();
		model.addAttribute("organizationList", organizationList);
		model.addAttribute("organizationPost", organizationPost);
		model.addAttribute("id", orgId);
		return "main/organizationView";
	}

	@GetMapping("/notification/detail/{id}")
	public String getNotificationDetail(@PathVariable("id") Long id, Model model, HttpServletRequest req) {
		addUserToModel(req, model);
		Notification notification = notificationService.selectById(id);
		model.addAttribute("notification", notification);
		return "main/notification-detail";
	}

	// 공통 코드 중복 제거
	private void addUserToModel(HttpServletRequest req, Model model) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		model.addAttribute("user", user);
	}
}
