package hapum.hapum.domain;

import lombok.Data;

@Data
public class BlockedDay {
    private int id;
    private int weekOfMonth;   // 1~5
    private int dayOfWeek;     // 0~6
    private String reason;
}