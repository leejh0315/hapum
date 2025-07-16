package hapum.hapum.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProgramAdd {

	private Long id;
	private String name;
	private String content;
	private String thumbnailSrc;
	private String openStatus;
}
