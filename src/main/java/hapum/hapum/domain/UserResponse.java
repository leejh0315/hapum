package hapum.hapum.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
	private Long id;
	private String name;
	private String baptismName;
}
