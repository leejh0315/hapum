package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrganizationPost {
    private Long id;
    private Long orgId;
    private String title;
    private String content;
    private String thumbnailSrc;
    private String openStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}