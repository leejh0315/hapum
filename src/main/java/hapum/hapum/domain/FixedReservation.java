package hapum.hapum.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class FixedReservation {
    private int id;
    private String type; // WEEKLY, MONTHLY
    private Integer weekday;       // 0~6
    private Integer weekOfMonth;   // 1~5
    private Integer dayOfWeek;     // 0~6
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDate;
}