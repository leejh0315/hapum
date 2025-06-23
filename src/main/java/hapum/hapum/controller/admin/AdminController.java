package hapum.hapum.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.controller.user.SummernoteController;
import hapum.hapum.domain.News;
import hapum.hapum.domain.Notification;
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationPost;
import hapum.hapum.domain.User;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.NotificationService;
import hapum.hapum.service.OrganizationService;
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

	@PostConstruct
	public void init() throws IOException {
		tempDir = Paths.get(tempVideoDirStr);
		videoDir = Paths.get(uploadVideoDirStr);
		Files.createDirectories(tempDir);
	}

	@GetMapping("/main")
	public String getAdminPage() {
		return "admin/main";
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
		System.out.println(organizationPost);
		String updatedContent = newsService.moveTempImagesToPosts(organizationPost.getContent(), "organizationPost");
		organizationPost.setContent(updatedContent);
		organizationService.insertOrganizationWrite(organizationPost, photo);
		return "admin/main";
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

	@PostMapping("/news/update/{newsId}")
	public String updateNews(@PathVariable("newsId") Long newsId, @RequestParam("code") String code) {
		if (code.equals("Y")) {
			code = "N";
		} else {
			code = "Y";
		}
		newsService.updateNews(newsId, code);

		System.out.println("*********************");

		return "redirect:/main/news";
	}

	@GetMapping("/currentRental")
	public String getCurrentRental() {
		return "admin/currentRental";
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

}
