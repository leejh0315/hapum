package hapum.hapum.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SigninForm {

	@NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
	private String email;


	private boolean emailChecked = false; 
	private boolean emailVerificationPassed = false;
	
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$", message = "비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.")
	private String password;

	@NotBlank(message = "비밀번호 확인은 필수입니다.")
	private String passwordCheck;

	@NotBlank(message = "이름은 필수입니다.")
	private String name;

	@Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자 (예: 20001225) 형식이어야 합니다.")
	private String birth;

	@Pattern(regexp = "^\\d{10,11}$", message = "연락처는 '-' 없이 10~11자리 숫자여야 합니다.")
	private String phone;

	@NotBlank(message = "본당명을 입력해주세요.")
	private String cathedral;

	@NotBlank(message = "세례명을 입력해주세요.")
	private String baptismName;
	
}
