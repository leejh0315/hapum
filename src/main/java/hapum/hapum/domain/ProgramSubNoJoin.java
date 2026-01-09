package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProgramSubNoJoin {
    private Long id;
    private Long programId;
    // getters/setters
    private String isApp;
    private User user;
    private LocalDateTime subsOn;	
    
    private String orgName; 
    private int partCount;
    private String relation;
    
    private String reason;
    private String opinion;
    
    
    private String name;
    private String baptismName;
    private String phone;
    private String parish;
    
}