package hapum.hapum.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import hapum.hapum.domain.SigninForm;
import hapum.hapum.domain.UpdateForm;
import hapum.hapum.domain.User;
import hapum.hapum.mapper.FixedReservationMapper;
import hapum.hapum.mapper.ProgramMapper;
import hapum.hapum.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils; 
    
    private final FixedReservationMapper frMapper;
    private final ProgramMapper programMapper;
    
    
    public boolean processSignup(SigninForm signinForm, BindingResult bindingResult) {

        // 비밀번호 확인
        if (!signinForm.getPassword().equals(signinForm.getPasswordCheck())) {
            bindingResult.rejectValue("passwordCheck", "error.passwordCheck", "비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 확인 여부
        if (!signinForm.isEmailChecked()) {
            bindingResult.rejectValue("email", "error.emailChecked", "이메일 중복 확인은 필수입니다.");
        }

        // 이메일 인증번호 확인 여부
        if (!signinForm.isEmailVerificationPassed()) {
            bindingResult.rejectValue("email", "error.emailVerificationPassed", "이메일 인증은 필수입니다.");
        }

        // 에러 있으면 false 반환
        if (bindingResult.hasErrors()) {
            return false;
        }

        // 비밀번호 암호화 및 저장
        String newPw = passwordEncoder.encode(signinForm.getPassword());
        signinForm.setPassword(newPw);

        int result = userMapper.userSignin(signinForm);

        return result == 1;
    }
    
    public int checkEmail(String email) {
        // 이메일 정규식 유효성 검사
        if (!Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", email)) {
            return 2; // 유효하지 않은 이메일 형식
        }

        int count = userMapper.loginIdCheck(email); // 이메일 중복 확인 쿼리
        return count > 0 ? 1 : 0; // 1: 중복됨, 0: 사용 가능
    }

    public User doLogin(String loginId, String password, BindingResult bindingResult) {
    	  if (bindingResult.hasErrors()) {
              return null;
          }
  		User user = userMapper.selectByEmail(loginId);
  		if(user != null) {
			if(passwordEncoder.matches(password, user.getPassword())) {
				return user;
			}
		}
		return null;
    }

    public int numberCheck(Map<String, String> payload) {
    	String userEmail = payload.get("email");
        String number = payload.get("number");

    	if(number.equals(redisUtils.getData(userEmail))) {
			redisUtils.setDataExpire(userEmail, "Y", 60*10L);
			return 1;
		}else if(redisUtils.getData(userEmail) == "" || redisUtils.getData(userEmail) == null) {
			return 2;
		}else {
			return 0;
		}
    }
    
    public List<User> selectAllUser(){
    	return userMapper.selectAllUser();
    }
    public User selectById(Long id) {
    	return userMapper.selectById(id);
    }
    
    public void updateUser(UpdateForm updateForm) {
    	userMapper.updateUserById(updateForm);
    }
    
    @Async
    public void updatePasswordByEmail(String email, CompletableFuture<String> newPw) {
    	String newPassword = newPw.join();
    	userMapper.updatePasswordByEmail(email, newPassword);
    }
    
    
    public void updatePassword(Long userId, String newPassword) {
        // 새 비밀번호를 암호화 처리
        String encodedPassword = passwordEncoder.encode(newPassword);
        // Mapper를 이용하여 DB의 비밀번호를 업데이트
        userMapper.updatePasswordByUserId(userId, encodedPassword);
    }
    
    public void changeStatusToInactive(Long userId, String currentStatus) {
    	if(currentStatus.equals("Y")){
    		currentStatus = "N";
    	}else {
    		currentStatus = "Y";
    	}
    	userMapper.changeStatusToInactive(userId, currentStatus);
    }
    
    public void deleteUserById(Long userId) {
    	//////////////////회원삭제 추가
    	userMapper.deleteUserById(userId);
    }
    
    @Transactional
    public void out(Long id) {
    	frMapper.deleteByUserId(id);
    	programMapper.deleteByUserId(id);
    	
    	userMapper.deleteUserById(id);
    }
    
}