package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Post {
	private Long id;
	private String title;
	private String content;
	private Long userId;
	private LocalDateTime createOn;
	private LocalDateTime updateOn;
}
