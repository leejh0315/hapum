package hapum.hapum.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.domain.News;
import hapum.hapum.domain.Notification;
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationOrderDto;
import hapum.hapum.domain.OrganizationPost;
import hapum.hapum.domain.Program;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.User;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.OrganizationService;
import hapum.hapum.service.ProgramService;
import hapum.hapum.service.ReservationService;
import hapum.hapum.service.UserAuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	@Value("${file.upload-dir}")
	private String uploadDir; // e.g. "./uploads"

	@Value("${summernote.upload.temp-dir}")
	private String tempVideoDirStr;

	@Value("${summernote.upload.video-dir}")
	private String uploadVideoDirStr;

	private Path tempDir;
	private Path videoDir;
	private final NewsService newsService;

	private final NotificationService notificationService;

	private final UserAuthService userAuthService;
	private final OrganizationService organizationService;
	private final ReservationService reservationService;

	private final ProgramService programService;

	@PostConstruct
	public void init() {
		tempDir = Paths.get(tempVideoDirStr);
		videoDir = Paths.get(uploadVideoDirStr);

		try {
			// temp 디렉토리 생성 (이미 있으면 무시)
			Files.createDirectories(tempDir);
			// upload 비디오 디렉토리도 생성
			Files.createDirectories(videoDir);
		} catch (IOException e) {
			// 체크 예외는 IllegalStateException 등 런타임 예외로 감싸서 던지기
			throw new IllegalStateException("디렉터리 생성 실패: " + tempVideoDirStr + " or " + uploadVideoDirStr, e);
		}

	}

	@GetMapping("/main")
	public String getAdminPage(@RequestParam(name="weekOffset", defaultValue="0") int weekOffset,
			Model model) {
		
		  // 2) 이번 주(또는 weekOffset 주차)의 시작/끝 날짜 계산
	    LocalDate today         = LocalDate.now();
	    LocalDate startOfWeek   = today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
	    LocalDate endOfWeek     = startOfWeek.plusDays(6);

	    DateTimeFormatter fmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    String weekStartStr     = startOfWeek.format(fmt);
	    String weekEndStr       = endOfWeek.format(fmt);

	    model.addAttribute("weekStart", weekStartStr);
	    model.addAttribute("weekEnd", weekEndStr);
		
		List<Program> weeklyPrograms = programService.getProgramsForWeek(weekOffset);
		
		
		
		model.addAttribute("reservations",
	            reservationService.getReservationsForWeek(weekOffset));
		model.addAttribute("programs", weeklyPrograms);
		model.addAttribute("weekOffset", weekOffset);
		model.addAttribute("prevWeek", weekOffset - 1);
		model.addAttribute("nextWeek", weekOffset + 1);
		return "admin/main";
	}

	@PostMapping("/notification/order")
	public String postNotificationOrder(@RequestBody List<Notification> notifications) {
		notificationService.updateOrder(notifications);
		return "redirect:/main/main";
	}

	@PostMapping("notification/toggleTop")
	public String postNotificationToggle(@RequestBody Notification notification) {
		if (notification.getIsTop().equals("1")) {
			notificationService.toggleTop(notification);
		} else {
			notification.setOrderNum(0);
			notificationService.toggleDown(notification);
		}
		return "redirect:/main/main";
	}

	@GetMapping("/notification")
	public String getNotification() {
		return "admin/notification";
	}

	// ────────── 3) 공지글 등록 시 temp → upload 로비디오 이동 ──────────
	@PostMapping("/writeNotification")
	public String writeNotification(@RequestParam("title") String title, @RequestParam("content") String content)
			throws IOException {

		// HTML 파싱
		Document doc = Jsoup.parseBodyFragment(content);
		// /temp/videos 로 시작하는 <video> 태그만 골라서 처리
		Elements videos = doc.select("video[src^=/temp/videos/]");
		for (Element v : videos) {
			String src = v.attr("src"); // ex) /temp/videos/uuid_파일명.mp4
			String filename = Paths.get(src).getFileName().toString();

			// 실제 파일 이동
			Path from = tempDir.resolve(filename);
			Path to = videoDir.resolve(filename);
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);

			// HTML 상의 src 경로 변경
			v.attr("src", "/uploads/videos/" + filename);
		}

		// 이미지 이동 등 추가 작업 (필요시)
		String finalHtml = doc.body().html();
		// 예) moveTempImagesToPosts 는 이미지도 같은 방식으로 옮겨주는 메서드
		String updatedContent = newsService.moveTempImagesToPosts(finalHtml, "notification");

		// DB에 저장
		Notification n = new Notification();
		n.setTitle(title);
		n.setContent(updatedContent);
		notificationService.insertNotification(n);

		return "redirect:/admin/main";
	}

	@GetMapping("/organization")
	public String getOrganization(Model model) {
		List<Organization> organizations = organizationService.selectAllOrganizationAdmin();
		model.addAttribute("organizations", organizations);
		return "admin/organization";
	}
	
	@GetMapping("/organization/update/{orgId}")
	public String getUpdateOrganization(@PathVariable("orgId")Long orgId, Model model) {
		Organization organization = organizationService.selectOrganizationById(orgId);
		model.addAttribute("organization", organization);
		return "admin/updateOrganization";
	}	
	@PostMapping("/organization/update/{orgId}")
	public String updateOrganization(@PathVariable("orgId") Long orgId,
	                                 Organization organization,
	                                 @RequestParam("photo") MultipartFile photo) throws IOException {
		organization.setId(orgId); // ID 수동 세팅
	    
	    organizationService.updateOrganization(organization, photo);
	    return "redirect:/admin/organization";
	}
	@PostMapping("organization/delete/{orgId}")
	public String deleteOrganization(@PathVariable("orgId")Long orgId) {
		organizationService.deleteOrganizationAndPosts(orgId);
		return "redirect:/admin/organization";
	}
	

	@GetMapping("/organization/add")
	public String getOrganizationAdd(Model model) {
		return "admin/organization-add";
	}

	@GetMapping("/organization/write")
	public String getOrganizationWrite(Model model) {
		List<Organization> organizationList = organizationService.selectAllOrganization();
		model.addAttribute("organizationList", organizationList);
		return "admin/organization-write";
	}

	@PostMapping("/organization/add")
	public String postOrganizationAdd(Organization organization, @RequestParam("photo") MultipartFile photo) {
		organizationService.insertOrganization(organization, photo);
		return "redirect:/admin/organization";
	}

	@PostMapping("/organization/write")
	public String postOrganizationWrite(OrganizationPost organizationPost, @RequestParam("photo") MultipartFile photo) {
		String updatedContent = newsService.moveTempImagesToPosts(organizationPost.getContent(), "organizationPost");
		organizationPost.setContent(updatedContent);
		organizationService.insertOrganizationWrite(organizationPost, photo);
		return "redirect:/admin/organization";
	}

	@PostMapping("/organization/updateCode/{orgId}")
	public String deleteOrganization(@PathVariable("orgId") Long orgId, @RequestParam("statusCode") String code) {
		if (code.equals("Y")) {
			code = "N";
		} else {
			code = "Y";
		}
		organizationService.deleteOrganization(orgId, code);
		return "redirect:/admin/organization";
	}

	@PostMapping("/organizationPost/delete/{orgPostId}")
	public String deleteOrganizationPost(@PathVariable("orgPostId") Long orgPostId) {
		organizationService.deleteOrganizationPost(orgPostId);
		return "redirect:/admin/organization";
	}
	
	@GetMapping("/org/updateOrgPost/{id}")
	public String getUpdateOrgPost(@PathVariable("id")Long id ,Model model) {
		OrganizationPost op = organizationService.selectOrgPostById(id);
		model.addAttribute("op", op);
		return "admin/updateOrgPost";
	}
	
	@PostMapping("/org/updateOrgPost/{id}")
	public String updateOrgPost(
	        @PathVariable("id") Long id,
	        OrganizationPost op,
	        @RequestParam("photo") MultipartFile photo
	        ) throws IOException {

	    op.setOrgId(id);
	    organizationService.updateOrganizationPost(op, photo);
	    
	    return "redirect:/main/organization";
	}
	@PostMapping("/org/delete/{id}")
	public String deleteOrgPost(@PathVariable("id") Long id) {
		organizationService.deleteOrganizationPost(id);
		return "redirect:/main/organization"; 
	}
	
	

	@GetMapping("/updateHistory")
	public String getUpdateHistory() {
		return "admin/updateHistory";
	}

	@GetMapping("/writeNews")
	public String getWriteNews() {

		return "admin/writeNews";
	}

	@PostMapping("/writeNews")
	public String writeNews(News news, @RequestParam("photo") MultipartFile photo) {
		String updatedContent = newsService.moveTempImagesToPosts(news.getContent(), "news");
		news.setContent(updatedContent);
		newsService.insertNews(news, photo);
		return "redirect:/admin/main";
	}

	@PostMapping("/news/delete/{newsId}")
	public String deleteNews(@PathVariable("newsId") Long newsId, @RequestParam("code") String code) {
		newsService.deleteNews(newsId, code);
		return "redirect:/main/news";
	}
	
	@GetMapping("/news/updateNews/{id}")
	public String updateNewsDetail(@PathVariable("id")Long id, Model model) {
		News news = newsService.selectById(id);
		model.addAttribute("news",news);
		return "admin/updateNewsDetail";
	}
	
	@PostMapping("/news/updateNewsDetail/{id}")
	public String postUpdateNewsDetail(@PathVariable("id")Long id,News news, @RequestParam("photo") MultipartFile photo) {
		news.setId(id);
		newsService.updateNewsDetail(news, photo);
		
		return "redirect:/main/news/detail/"+id;
	}
	

	@GetMapping("/currentRental")
	public String getCurrentRental(Model model) {

		List<Rental> rentals = reservationService.selectAllRentals();
		model.addAttribute("rentals", rentals);
		return "admin/currentRental";
	}

	@PostMapping("/rental/approve/{id}")
	public String postApprove(@PathVariable("id") Long id) {
		reservationService.approve(id);
		return "redirect:/admin/currentRental";
	}

	@PostMapping("/rental/delete/{id}")
	public String postDelete(@PathVariable("id") Long id) {
		reservationService.delete(id);
		return "redirect:/admin/currentRental";
	}

	@PostMapping("/rental/disapprove/{id}")
	public String postDisapprove(@PathVariable("id") Long id) {
		reservationService.disapprove(id);
		return "redirect:/admin/currentRental";
	}

	@GetMapping("/allUser")
	public String getAllUser(Model model) {
		List<User> users = userAuthService.selectAllUser();
		model.addAttribute("users", users);
		return "admin/allUser";
	}

	@PostMapping("/changeStatus/{id}")
	public String changeUserStatus(@PathVariable("id") Long id, @RequestParam("statusCode") String currentStatus) {

		userAuthService.changeStatusToInactive(id, currentStatus); // service 내에서 statusCode를 'N'으로 업데이트
		return "redirect:/admin/allUser"; // 관리자 회원 목록 페이지로 리다이렉트 처리
	}

	// 회원 삭제 처리
	@PostMapping("/delete/{id}")
	public String deleteUser(@PathVariable("id") Long id) {
		userAuthService.deleteUserById(id); // 회원 삭제 처리
		return "redirect:/admin/allUser";
	}
	
	@PostMapping("/organization/updateOrder")
	@ResponseBody
	public ResponseEntity<Void> updateOrder(@RequestBody List<OrganizationOrderDto> orderList) {
	    organizationService.updateOrganizationOrder(orderList);
	    return ResponseEntity.ok().build();
	}
	
	@PostMapping("/notification/delete/{id}")
	public String deleteNotification(@PathVariable("id") Long id) {
		notificationService.deleteByNotification(id);		
		return "redirect:/";
	}
	
	@GetMapping("/notification/updateNoti/{id}")
	public String updateNotification(@PathVariable("id")Long id, Model model) {
		
		Notification notification = notificationService.selectById(id);
		model.addAttribute("notification", notification);
		
		return "admin/updateNotification";
	}
	
	@PostMapping("/notification/updateNotification/{id}")
	public String postUpdateNoti(@PathVariable("id")Long id, Notification notificaton) {
		notificationService.updateNotification(notificaton);
		return "redirect:/main/notification/detail/"+id.toString();
	}
	
	
	

}
