package hapum.hapum.domain;

import java.time.LocalTime;

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
}