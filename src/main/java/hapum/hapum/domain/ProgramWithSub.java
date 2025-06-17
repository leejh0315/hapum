package hapum.hapum.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true) 
public class ProgramWithSub extends Program {
    // Program의 필드에 추가해서 ProgramSub의 subsOn 값을 저장
    private Long subsId;
	private LocalDateTime subsOn;
    private String isApp;
}
