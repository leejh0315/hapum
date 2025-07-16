package hapum.hapum.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RentalWithUser extends Rental {
	 private String email;
	    private String name;
	    private String birth;
	    private String phone;
	    private String cathedral;
	    private String baptismName;
	    private String statusCode;
}
