package hapum.hapum.domain;

import lombok.Data;

@Data
public class RentalWithUser extends Rental {
	 private String email;
	    private String name;
	    private String birth;
	    private String phone;
	    private String cathedral;
	    private String baptismName;
	    private String statusCode;
}
