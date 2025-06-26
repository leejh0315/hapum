package hapum.hapum.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
@Data
public class UpdateForm {
	
	private Long id;
	
	@NotBlank(message = "이름은 필수입니다.")
	 @Size(min = 2, max = 30,
          message = "이름은 최소 {min}자, 최대 {max}자까지 입력 가능합니다.")
	private String name;

	@Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자 (예: 20001225) 형식이어야 합니다.")
	private String birth;

	@Pattern(regexp = "^\\d{10,11}$", message = "연락처는 '-' 없이 10~11자리 숫자여야 합니다.")
	private String phone;

	@NotBlank(message = "본당명을 입력해주세요.")
	 @Size(min = 3, max = 50,
          message = "본당명은 최소 {min}자, 최대 {max}자까지 입력 가능합니다.")
	private String cathedral;

	@NotBlank(message = "세례명을 입력해주세요.")
	 @Size(min = 2, max = 30,
          message = "세례명은 최소 {min}자, 최대 {max}자까지 입력 가능합니다.")
	private String baptismName;
}
