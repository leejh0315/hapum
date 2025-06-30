package hapum.hapum.controller.user;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hapum.hapum.domain.LoginForm;
import hapum.hapum.domain.SigninForm;
import hapum.hapum.domain.User;
import hapum.hapum.service.EmailService;
import hapum.hapum.service.SessionService;
import hapum.hapum.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserAuthController {

	private final UserAuthService userAuthService;
	private final SessionService sessionService;
	private final EmailService emailService;

	@GetMapping("/login")
	public String getLogin(Model model) {
		model.addAttribute("loginForm", new LoginForm()); // 모델에 객체 추가
		return "auth/login";
	}

	@PostMapping("/doLogin")
	public String doLogin(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult, Model model,
			HttpServletRequest req, HttpServletResponse resp) {
		User user = userAuthService.doLogin(loginForm.getLoginId(), loginForm.getPassword(), bindingResult);
		if (user == null) {
			model.addAttribute("fail", "fail");
			return "auth/login";
		} else {
			sessionService.create(user, resp);

			HttpSession session = req.getSession(true);// 세션에 정보가 없을 때, null을 반환하는 것이 아닌 새로운 객체를 생성하여 반환
			session.setAttribute("loginMember", user);
			if(user.getStatusCode().equals("N")) {
				session.invalidate();
	            // 모델에 에러 메시지 추가
	            model.addAttribute("inactiveMsg", "회원님의 계정은 현재 비활성 상태입니다. 관리자에게 문의하시기 바랍니다.");
	            return "auth/login";
			}
			
			return "redirect:/main/main";
		}
	}

	@GetMapping("/success")
	public String showSignupSuccessPage(@ModelAttribute("fromSignup") String fromSignup) {
		if (!"true".equals(fromSignup)) {
			return "redirect:/main/main"; // 혹은 404 페이지 등
		}
		return "auth/success";
	}

	@PostMapping("/emailCheck")
	public ResponseEntity<Integer> checkEmail(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		int result = userAuthService.checkEmail(email);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/authEmail")
	public ResponseEntity<String> authEmail(@RequestBody Map<String, String> payload) throws Exception {
		String email = payload.get("email");
		emailService.sendMessage(email);

		return ResponseEntity.ok("");
	}

	@ResponseBody
	@PostMapping("/numberCheck")
	public String numberCheck(@RequestBody Map<String, String> payload) {
		int num = userAuthService.numberCheck(payload);
		return String.valueOf(num);
	}

	@GetMapping("/signin")
	public String getSignin(Model model) {
		model.addAttribute("signinForm", new SigninForm()); // 모델에 객체 추가
		return "auth/signin";
	}

	@PostMapping("/doSignin")
	public String processSignup(@Valid @ModelAttribute SigninForm signinForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, Model model) {

		boolean success = userAuthService.processSignup(signinForm, bindingResult);

		if (!success) {
			model.addAttribute("signinForm", signinForm);
			return "auth/signin";
		}

		redirectAttributes.addFlashAttribute("fromSignup", "true");
		return "redirect:/auth/success";
	}

	@PostMapping("/doLogout")
	@ResponseBody
	public String doLogout(HttpServletRequest req, @RequestBody Map<String, String> payload) {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		// 클라이언트에서 redirect할 주소를 JSON으로 응답

		return payload.get("link");
	}

	@GetMapping("/findPw")
	public String getFindPw() {
		return "auth/findPw";
	}

	@PostMapping("/findPw")
	@ResponseBody
	public int postFindPw(@RequestParam("email") String email) throws Exception {
		int count = userAuthService.checkEmail(email);
		if (count == 0) {
			return count;
		}
		System.out.println(email);
		CompletableFuture<String> newPw = emailService.sendNewPassword(email);
		userAuthService.updatePasswordByEmail(email, newPw);
		return count;
	}

}
