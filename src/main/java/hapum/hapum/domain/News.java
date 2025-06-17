package hapum.hapum.domain;

import lombok.Data;

@Data
public class News {
    private Long id;
    private String title;
    private String content;
    private String thumbnailSrc;
    // getters/setters
}
