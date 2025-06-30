package hapum.hapum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.SigninForm;
import hapum.hapum.domain.UpdateForm;
import hapum.hapum.domain.User;

@Mapper
public interface UserMapper {
	
	public int userSignin(SigninForm signinForm); 
	public int loginIdCheck(String loginId);
	public User selectByEmail(String loginId);
//	public User doLogin(User user);
	
	public List<User>selectAllUser();
	
	public User selectById(@Param("id")Long id);
	public void updateUserById(UpdateForm updateForm);
	public void updatePasswordByEmail(@Param("email")String email, @Param("password")String newPw);
	
	void updatePasswordByUserId(@Param("userId") Long userId, @Param("password") String encodedPassword);
	
	
	public void changeStatusToInactive(@Param("userId")Long userId, @Param("code") String currentStatus ); 
    public void deleteUserById(@Param("userId")Long userId); 
}
