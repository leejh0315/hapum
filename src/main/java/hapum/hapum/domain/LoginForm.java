package hapum.hapum.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm {

	@NotBlank(message = "아이디는 필수입니다.")
	private String loginId;

	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;
}
