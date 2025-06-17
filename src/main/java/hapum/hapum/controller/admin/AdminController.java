package hapum.hapum.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationPost;
import hapum.hapum.domain.User;
import hapum.hapum.service.NewsService;
import hapum.hapum.service.OrganizationService;
import hapum.hapum.service.UserAuthService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	
	private final NewsService newsService;
	
	@Autowired
	private SummernoteController summernoteController;
	
	private final UserAuthService userAuthService;
	private final OrganizationService organizationService; 
	
	@GetMapping("/main")
	public String getAdminPage() {
		return "admin/main";
	}
	
	@GetMapping("/organization")
	public String getOrganization(Model model) {
		List<Organization> organizations = organizationService.selectAllOrganization();
		
		model.addAttribute("organizations", organizations);
		return "admin/organization";
	}
	@GetMapping("/organization/add")
	public String getOrganizationAdd(Model model) {
		
		return "admin/organization-add";
	}
	
	@GetMapping("/organization/write")
	public String getOrganizationWrite(Model model) {
		List<Organization> organizationList = 
				organizationService.selectAllOrganization();
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
		String updatedContent = summernoteController.moveTempImagesToPosts(organizationPost.getContent(), "organizationPost");
		organizationPost.setContent(updatedContent);
		organizationService.insertOrganizationWrite(organizationPost, photo);
		return "admin/main";
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
		String updatedContent = summernoteController.moveTempImagesToPosts(news.getContent(), "news");
		news.setContent(updatedContent);
		newsService.insertNews(news, photo);
	    return "redirect:/admin/main";
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
		 
		 userAuthService.changeStatusToInactive(id, currentStatus);  // service 내에서 statusCode를 'N'으로 업데이트
	        return "redirect:/admin/allUser";  // 관리자 회원 목록 페이지로 리다이렉트 처리
	    }
	 

	    
	    // 회원 삭제 처리
	    @PostMapping("/delete/{id}")
	    public String deleteUser(@PathVariable("id") Long id) {
	    	userAuthService.deleteUserById(id);  // 회원 삭제 처리
	        return "redirect:/admin/allUser";
	    }
	
	
}
