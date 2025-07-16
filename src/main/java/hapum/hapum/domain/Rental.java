package hapum.hapum.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Rental {
    private Long id;                     // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String location;             // VARCHAR(100) NOT NULL, 대관장소
    private LocalDate reservationDate;   // DATE NOT NULL, 대관날짜
    private LocalTime startTime;         // TIME NOT NULL, 대관시작시간
    private LocalTime endTime;           // TIME NOT NULL, 대관끝나는시간
    private String price;            // DECIMAL(10,2) NOT NULL, 대관료
    private Long userId;                 // BIGINT NOT NULL, 대관신청한 user의 id (외래키)
    private LocalDateTime createdOn;     // DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 대관신청한 날짜
    private String isApp;            // TINYINT(1) NOT NULL DEFAULT 0, 승인여부 (0: 미승인, 1: 승인)
    private String purpose;
    private String groupName;
    private String equipment;
    private int headCount;
}
