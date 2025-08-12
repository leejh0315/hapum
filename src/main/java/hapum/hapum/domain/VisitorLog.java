package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VisitorLog {
    private Long id;
    private String sessionId;
    private LocalDateTime visitTime;
}
