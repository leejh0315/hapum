package hapum.hapum.controller;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import hapum.hapum.domain.BlockedDay;
import hapum.hapum.domain.FixedReservation;
import hapum.hapum.domain.News;
import hapum.hapum.domain.Notification;
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationPost;
import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramSub;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.User;
import hapum.hapum.service.EmailService;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.OrganizationService;
import hapum.hapum.service.ProgramService;
import hapum.hapum.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

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
	
	@Value("${file.upload-dir}")
	private String uploadDir; // e.g. "./uploads"
	
	
	@GetMapping("/main")
	public String getMain(HttpServletRequest req, Model model) {
		addUserToModel(req, model);
		List<Program> programs = programService.selectAllPrograms();
		
		File folder = new File(uploadDir+"/news/");
        File[] files = folder.listFiles();  
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {

                    fileNames.add("/uploads/news/"+f.getName());
                }
            }
        }
        
		
		List<Map<String, Object>> eventList = programs.stream().map(program -> {
		    Map<String, Object> event = new HashMap<>();
		    event.put("id", program.getId());
		    event.put("title", program.getTitle());
		    // ISO-8601 형식: yyyy-MM-dd'T'HH:mm
		    event.put("start", program.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
		    event.put("end", program.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
		    event.put("thumbnail", program.getThumbnailSrc());
		    event.put("url", "/main/program/detail/" + program.getId());
		    
		    return event;
		}).collect(Collectors.toList());

		try {
		    String eventsJson = new ObjectMapper().writeValueAsString(eventList);
		    
		    model.addAttribute("eventsJson", eventsJson);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		List<Notification> notifications =
		notificationService.selectAll();
		
		model.addAttribute("fileNames", fileNames);
		model.addAttribute("programs", programs);
		model.addAttribute("notifications",notifications);
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
		List<Program> programList = programService.selectAllPrograms();
		model.addAttribute("programs", programList);
		return "main/program";
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

		ProgramSub ps = new ProgramSub();
		ps.setUserId(user.getId());
		ps.setProgramId(program.getId());

		int temp = programService.programSub(ps);

		emailService.sendProgramMessage(user.getEmail(), program);
		
		return temp;
	}

	@GetMapping("/rental")
	public String getRental(HttpServletRequest req, Model model) {
		addUserToModel(req, model);

		List<BlockedDay> blockedDays = reservationService.getBlockedDays();
		List<FixedReservation> fixedReservations = reservationService.getFixedReservations();
		List<Rental> rentals = reservationService.getRentals();


		model.addAttribute("blockedDays", blockedDays);
		model.addAttribute("fixedReservations", fixedReservations);
		model.addAttribute("rentals", rentals);
		return "main/rental";
	}

	@PostMapping("/submitRental")
	@ResponseBody
	public String submitRental(@RequestBody Rental rental, HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		rental.setUserId(user.getId());
		reservationService.insertRental(rental);
		emailService.sendRentalMessage(user.getEmail(), rental);

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
		model.addAttribute("organizationList",organizationList);
		return "main/organizationList";
	}

	@GetMapping("/organization/{id}")
	public String getOrganizationDetail(@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,Model model, HttpServletRequest req, @PathVariable("id")Long id) {
		addUserToModel(req, model);
		List<Organization> organizationList = organizationService.selectAllOrganization();
		
		List<OrganizationPost> organizationPost = organizationService.selectByOrgId(id, page,size);
		int totalOrgPost = organizationService.getTotalOrgPost(id);
		int totalPages = (int) Math.ceil((double) totalOrgPost / size);
		
		
		
		model.addAttribute("organizationList",organizationList);
		model.addAttribute("organizationPost", organizationPost);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("id", id);
		return "main/organizationDetail";
	}
	
	@GetMapping("/organization/{orgId}/post/{id}")
	public String getOrganizationPost(HttpServletRequest req, Model model, @PathVariable("id")Long id, @PathVariable("orgId") Long orgId) {
		addUserToModel(req, model);
		OrganizationPost organizationPost = organizationService.selectOrgPostById(id);
		List<Organization> organizationList = organizationService.selectAllOrganization();
		model.addAttribute("organizationList",organizationList);
		model.addAttribute("organizationPost", organizationPost);
		model.addAttribute("id", orgId);
		return "main/organizationView";
	}
	
	@GetMapping("/notification/detail/{id}")
	public String getNotificationDetail(@PathVariable("id")Long id, Model model, HttpServletRequest req) {
		addUserToModel(req, model);
		Notification notification = notificationService.selectById(id);
		model.addAttribute("notification",notification);
		return "main/notification-detail";
	}
	
	

	// 공통 코드 중복 제거
	private void addUserToModel(HttpServletRequest req, Model model) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		model.addAttribute("user", user);
		System.out.println("This user : " + user);
	}
}
