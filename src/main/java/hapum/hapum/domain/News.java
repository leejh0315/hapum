package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class News {
    private Long id;
    private String title;
    private String content;
    private String thumbnailSrc;
    // getters/setters
    
    private LocalDateTime createOn;
    private LocalDateTime updateOn;
    private String openStatus;
}
