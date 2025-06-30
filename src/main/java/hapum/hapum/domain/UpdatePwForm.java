package hapum.hapum.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePwForm {
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$", message = "비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.")
	private String password;

	@NotBlank(message = "비밀번호 확인은 필수입니다.")
	private String passwordCheck;
}
