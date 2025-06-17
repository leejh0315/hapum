package hapum.hapum.domain;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class Program {

    private Long id;
    private String title;       // 제목
    private String content;     // 내용
    private String subject;		//주제
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDate;
    
    private String location;    // 장소
    private String target;      // 대상
    private Integer capacity;   // 정원
    private String expense; 		//참가비
    private String thumbnailSrc;
    
    private LocalDateTime createOn;
    private LocalDateTime updateOn;
}
