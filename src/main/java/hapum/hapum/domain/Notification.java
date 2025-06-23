package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Notification {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createOn;
    private LocalDateTime updateOn;
    // getters/setters
}
