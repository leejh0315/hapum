package hapum.hapum.controller;

import java.util.List;
import java.util.Map;
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
import hapum.hapum.domain.ProgramSubNoJoin;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.User;
import hapum.hapum.service.ConvertService;
import hapum.hapum.service.EmailService;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.OrganizationService;
import hapum.hapum.service.ProgramService;
import hapum.hapum.service.ReservationService;
import hapum.hapum.service.VisitorService;
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
	private final VisitorService visitorService;

	private final ConvertService convertService;

	@Value("${file.upload-dir}")
	private String uploadDir; // e.g. "./uploads"

	@GetMapping("/main")
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

	    int applyCount;
	    List<ProgramSub> ps = programService.selectPrograSubmById(id);
	    List<ProgramSubNoJoin> psNoJoin = null;

	    // ✅ needJoin 여부에 따라 신청자 수 계산
	    if ("Y".equals(program.getNeedJoin())) {
	        applyCount = programService.getApplyCount(program.getId()); // 회원가입 신청자 수
	        ps = programService.selectPrograSubmById(id);
	    } else {
	        applyCount = programService.getApplyCountNoJoin(program.getId()); // 비회원 신청자 수
	    }

	    int remainingSeats = program.getCapacity() - applyCount;

	    String flag = "N";
	    if (user != null) {
	        // 로그인된 상태라면 needJoin 값과 상관없이 userId 기준으로 신청 여부 체크
	        if (ps != null && ps.stream().anyMatch(sub -> sub.getUserId() == user.getId())) {
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
	public int postProgramSubs(@RequestBody Map<String, Object> payload, HttpServletRequest req) throws Exception {
	    Long programId = Long.valueOf(payload.get("id").toString());
	    String needJoin = (String) payload.get("needJoin");

	    HttpSession session = req.getSession();
	    User sessionUser = (User) session.getAttribute("loginMember");

	    int applyCount = "Y".equals(needJoin)
	            ? programService.getApplyCount(programId)
	            : programService.getApplyCountNoJoin(programId);

	    // 공통 ProgramSub 생성
	    ProgramSub ps = new ProgramSub();
	    ps.setProgramId(programId);
	    ps.setOrgName((String) payload.get("needOrgName"));
	    ps.setReason((String) payload.get("needReason"));
	    ps.setOpinion((String) payload.get("needOpinion"));
	    ps.setRelation((String) payload.get("needRelation"));

	    // partCount 처리
	    String partCountStr = (String) payload.get("needPartCount");
	    if (partCountStr != null && !"N".equals(partCountStr)) {
	        int partCount = Integer.parseInt(partCountStr);
	        ps.setPartCount(partCount);
	        if ((payload.get("needCapacity") == null || !"Y".equals(payload.get("needCapacity")))
	                && ((Integer) payload.get("capacity") - applyCount < partCount)) {
	            return 2;
	        }
	    }

	    int temp;
	    User user;

	    if ("Y".equals(needJoin)) {
	        // Case 1 & 2
	        if (sessionUser == null) {
	            // 로그인 필요 → 실패 코드 반환 or 예외 처리
	            return -1; // 예: -1은 로그인 필요
	        }
	        user = sessionUser;
	        ps.setUserId(user.getId());

	        temp = programService.programSub(ps);

	        Program program = buildProgramFromPayload(payload, programId);
	        emailService.sendProgramMessage(user.getEmail(), program);

	        String result = convertService.generateProgramWordFromTemplate(ps, program, user);
	        emailService.sendEmailProgram("hapum7179@gmail.com", result);

	    } else {
	        // needJoin == "N"
	        if (sessionUser != null) {
	            // Case 3: 로그인된 상태에서 비회원 신청
	            user = sessionUser;
	            ps.setUserId(user.getId());
	            temp = programService.programSub(ps);
	            Program program = buildProgramFromPayload(payload, programId);
	            emailService.sendProgramMessage(user.getEmail(), program);
	            return temp;
	        } else {
	            // Case 4: 로그인 안 된 상태에서 비회원 신청
	            user = new User();
	            user.setName((String) payload.get("name"));
	            user.setBaptismName((String) payload.get("baptismName"));
	            user.setCathedral((String) payload.get("parish"));
	            user.setPhone((String) payload.get("phone"));
	        }

	        ProgramSubNoJoin psNoJoin = new ProgramSubNoJoin();
	        psNoJoin.setProgramId(programId);
	        psNoJoin.setOrgName((String) payload.get("needOrgName"));
	        psNoJoin.setReason((String) payload.get("needReason"));
	        psNoJoin.setOpinion((String) payload.get("needOpinion"));
	        psNoJoin.setRelation((String) payload.get("needRelation"));
	        if (partCountStr != null && !"N".equals(partCountStr)) {
	            psNoJoin.setPartCount(Integer.parseInt(partCountStr));
	        }
	        psNoJoin.setName(user.getName());
	        psNoJoin.setBaptismName(user.getBaptismName());
	        psNoJoin.setParish(user.getCathedral());
	        psNoJoin.setPhone(user.getPhone());

	        temp = programService.programSubWithNoJoin(psNoJoin);

	        Program program = buildProgramFromPayload(payload, programId);
	        String result = convertService.generateProgramWordFromTemplate(ps, program, user);
	        emailService.sendEmailProgram("hapum7179@gmail.com", result);
	    }

	    return temp;
	}

	// ✅ Program 객체 생성 보조 메소드
	private Program buildProgramFromPayload(Map<String, Object> payload, Long programId) {
	    Program program = new Program();
	    program = programService.selectProgramById(programId);
	    program.setId(programId);
	    program.setCapacity((Integer) payload.get("capacity"));
	    program.setNeedCapacity((String) payload.get("needCapacity"));
	    program.setNeedOrgName((String) payload.get("needOrgName"));
	    program.setNeedPartCount((String) payload.get("needPartCount"));
	    program.setNeedRelation((String) payload.get("needRelation"));
	    program.setNeedReason((String) payload.get("needReason"));
	    program.setNeedOpinion((String) payload.get("needOpinion"));
	    program.setNeedJoin((String) payload.get("needJoin"));
	    return program;
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

		log.info("User '{}' (ID: {}) submitted a rental request. Rental Info: {}", user.getName(), user.getId(),
				rental);

		emailService.sendRentalMessage(user.getEmail(), rental);

		String result = convertService.generateRentalWordFromTemplate(rental, user);
		emailService.sendEmailRental("hapum7179@gmail.com", result);
		// hapum7179@gmail.com
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
