package hapum.hapum.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {
    private Long id;

    private String email;
    private String password;
    private String name;
    private String birth;
    private String phone;
    private String cathedral;
    private String baptismName;
    private String statusCode;

	public UserResponse toResponseDTO() {
		return UserResponse.builder()
				.id(this.id)
				.name(this.name)
				.baptismName(this.baptismName)
				.build();
	}
}