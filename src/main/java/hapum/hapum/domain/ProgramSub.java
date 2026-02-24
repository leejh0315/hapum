package hapum.hapum.domain;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

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
    
    private String reason;
    private String opinion;
    
    private String name;            // 비회원 이름
    private String phone;           // 비회원 전화번호
    private String baptismName;     // 비회원 세례명
    private String parish;          // 소속 본당
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;
}