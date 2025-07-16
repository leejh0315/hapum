package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProgramSub {
    private Long id;
    private Long userId;
    private Long programId;
    // getters/setters
    private String isApp;
    private User user;
    private LocalDateTime subsOn;	
    
    private String orgName; 
    private int partCount;
    private String relation;
    
}