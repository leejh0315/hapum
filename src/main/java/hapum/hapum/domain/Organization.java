package hapum.hapum.domain;

import lombok.Data;

@Data
public class Organization {

	private Long id;
	private String name;
	private String introduce;
	private String profileSrc;
	private String openStatus;
}
