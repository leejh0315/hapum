package hapum.hapum.controller.user;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import hapum.hapum.domain.ProgramWithSub;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.UpdateForm;
import hapum.hapum.domain.UpdatePwForm;
import hapum.hapum.domain.User;
import hapum.hapum.service.ProgramService;
import hapum.hapum.service.ReservationService;
import hapum.hapum.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/main/mypage")
@RequiredArgsConstructor
public class MypageController {
	
	
	private final UserAuthService userAuthService;
	private final ReservationService reservationService;
	private final ProgramService programService;	
	
	
	@GetMapping("/{id}")
	public String getMypage(@PathVariable("id") Long id, Model model, HttpServletRequest req) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		if(mypageValidator(id, user, model)) {
			return "redirect:/main/main";
		}
		return "mypage/mypage";
	}
	
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model, HttpServletRequest req) {
    	HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
    	// 로그인한 사용자의 식별자(예: 이메일)를 통해 User 정보를 조회합니다.
    	
		if(mypageValidator(id, user, model)) {
			return "redirect:/main/main";
		}
    	
        // SigninForm 객체에 기존 사용자 정보를 채워 넣습니다.
        UpdateForm updateForm = new UpdateForm();
        updateForm.setName(user.getName());
        updateForm.setBirth(user.getBirth());
        updateForm.setPhone(user.getPhone());
        updateForm.setCathedral(user.getCathedral());
        updateForm.setBaptismName(user.getBaptismName());

        model.addAttribute("updateForm", updateForm);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "update");
        return "mypage/update";
    }
	
    @PostMapping("/update")
    public String postUpdate(
            @Valid @ModelAttribute UpdateForm updateForm,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest req) {

    	HttpSession session = req.getSession();
    	User user = (User) session.getAttribute("loginMember");
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("form", updateForm);
            model.addAttribute("activeTab", "update");
            return "mypage/update";
        }
        
        updateForm.setId(user.getId());
        userAuthService.updateUser(updateForm); // 실제 업데이트 로직 호출
        User refreshUser = userAuthService.selectById(user.getId());
        
		session.setAttribute("loginMember", refreshUser);
        
        
        return "redirect:/main/mypage/"+user.getId(); // 업데이트 후 메인 페이지로 리다이렉트
    }
	
	
	
	@GetMapping("/rental/{id}")
	public String getRental(@PathVariable("id") Long id, Model model, HttpServletRequest req) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		if(mypageValidator(id, user, model)) {
			return "redirect:/main/main";
		}
		
		List<Rental> rentals = reservationService.selectByUserId(user.getId());
		System.out.println(rentals);
		model.addAttribute("activeTab", "rental");
		model.addAttribute("rentals", rentals);
		
		return "mypage/rental";
	}
	
	@PostMapping("/rentalDelete/{id}")
	public String postRentalDelete(@PathVariable("id") Long id,HttpServletRequest req) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		reservationService.deleteRental(id);
		return "redirect:/main/mypage/rental/"+user.getId();
	}
	
	
	
	@GetMapping("/program/{id}")
	public String getProgram(@PathVariable("id") Long id, Model model, HttpServletRequest req) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		if(mypageValidator(id, user, model)) {
			return "redirect:/main/main";
		}
		List<ProgramWithSub> programs = programService.selectProgramByUserId(user.getId());
		model.addAttribute("programs", programs);
		model.addAttribute("activeTab", "program");
		return "mypage/program";
	}
	
	@PostMapping("/programDelete/{id}")
	public String postDeleteProgram(@PathVariable("id") Long id, HttpServletRequest req) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		
		programService.deleteByProgramSubsId(id);
		
		return "redirect:/main/mypage/program/"+user.getId();
	}
	
	@GetMapping("/updatePw/{id}")
	public String getUpdatePw(@PathVariable("id") Long id, Model model, HttpServletRequest req) {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("loginMember");
		if(mypageValidator(id, user, model)) {
			return "redirect:/main/main";
		}
		UpdatePwForm updatePwForm = new UpdatePwForm();
		model.addAttribute("updatePwForm", updatePwForm);
	
		
		return "mypage/updatePw";
	}
	
	 @PostMapping("/updatePw")
	    public String changePassword(@Valid @ModelAttribute("updatePwForm") UpdatePwForm updatePwForm,
	                                 BindingResult bindingResult,
	                                 HttpServletRequest request,
	                                 Model model) {
		 
		  HttpSession session = request.getSession();
	        User user = (User) session.getAttribute("loginMember");
	        if(user == null) {
	            // 로그인 세션이 없으면 로그인 페이지로 리다이렉트
	            return "redirect:/auth/login";
	        }
	        
	        // 유효성 검증 에러가 있으면 폼 페이지로 다시 전송
	        if (bindingResult.hasErrors()) {
	            return "mypage/updatePw";  // 비밀번호 수정 폼 뷰 (Thymeleaf 템플릿)
	        }
	        
	        // 비밀번호와 확인 필드 일치 여부 체크
	        if (!updatePwForm.getPassword().equals(updatePwForm.getPasswordCheck())) {
	            bindingResult.rejectValue("passwordCheck", "error.passwordCheck", "비밀번호 확인이 일치하지 않습니다.");
	            return "mypage/updatePw";
	        }
	      
	        System.out.println("성공");
	        userAuthService.updatePassword(user.getId(), updatePwForm.getPassword());
	        // 업데이트 후 사용자 프로필 등 원하는 페이지로 리다이렉트
	        
	        //로그아웃 시키고 메인으로 이동
	        
	        if (session != null) {
	            session.invalidate();
	        }
	        
	        
	        return "redirect:/main/main";
	    }
	private Boolean mypageValidator(Long thisPageId, User user ,Model model) {
		
		if(Long.toString(thisPageId).equals(Long.toString(user.getId()))) {
			model.addAttribute("user", user);
			return false;
		}else {
			return true;
		}
	}
}
