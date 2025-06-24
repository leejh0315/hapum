package hapum.hapum.service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hapum.hapum.domain.Program;
import hapum.hapum.domain.Rental;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	public static String ePw = createKey();
	private final RedisUtils redisUtils;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
    JavaMailSender emailSender;
    
    private MimeMessage createMessage(String to)throws Exception{
    	ePw = createKey();
        MimeMessage  message = emailSender.createMimeMessage();
        
        message.addRecipients(RecipientType.TO, to);//보내는 대상
        message.setSubject("비밀번호 발송");//제목
 
        String msgg="";
        msgg+= "<div style='margin:20px;'>";
        msgg+= "<h1> 안녕하세요 하품센터입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>아래의 인증번호를 입력해주세요.<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다.<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana; width: 500px'>";
        msgg+= "<h3 style='color:blue;'>회원가입 인증번호 입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= ePw+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("hapum7179@gmail.com","<HAPUM>"));//보내는 사람
        redisUtils.setDataExpire(to, ePw, 180);
        System.out.println(ePw);
        return message;
    }
 
    private MimeMessage newPassword(String to)throws Exception{
    	ePw = createKey();
        MimeMessage  message = emailSender.createMimeMessage();
        message.addRecipients(RecipientType.TO, to);//보내는 대상
        message.setSubject("임시 비밀번호 발송");//제목
 
        String msgg="";
        msgg+= "<div style='margin:20px;'>";
        msgg+= "<h1> 안녕하세요 하품센터입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>아래의 임시 비밀번호로 로그인 후, 비밀번호 변경을 해주세요.<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다.<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana; width: 500px'>";
        msgg+= "<h3 style='color:blue;'>임시 비밀번호입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "PASSWORD : <strong>";
        msgg+= ePw+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("hapum7179@gmail.com","<HAPUM>"));//보내는 사람
        
        return message;
    }
    
    
    private MimeMessage programMessage(String to, Program program)  throws Exception{
    	MimeMessage message = emailSender.createMimeMessage();
    	message.addRecipients(RecipientType.TO, to);//보내는 대상
        message.setSubject("프로그램 정보 발송");//제목
    	
    	String msgg = "";
    	msgg += "<div style='max-width:1000px;margin:50px auto;display:flex;flex-wrap:wrap;gap:30px;padding:20px;border:1px solid #ddd;border-radius:12px;background-color:#fffefa;'>";
    	msgg += "  <div style='flex:1 1 300px;'>";
    	msgg += "    <img src=' " + program.getThumbnailSrc() + "alt='프로그램 썸네일' style='width:100%;border-radius:12px;border:1px solid #ccc;' />";
    	msgg += "  </div>";
    	msgg += "  <div style='flex:1 1 400px;display:flex;flex-direction:column;justify-content:space-between;'>";
    	msgg += "    <h2 style='margin-bottom:16px;color:#333;'>" +program.getTitle() + "</h2>";
    	msgg += "    <ul style='list-style:none;padding:0;margin-bottom:20px;'>";
    	msgg += "      <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>일시:</strong>" + program.getStartDate() +"~"+program.getEndDate()+ "</li>";
    	msgg += "      <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>장소:</strong>"+ program.getLocation() +"</li>";
    	msgg += "      <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>대상:</strong> "+program.getTarget()+"</li>";
    	msgg += "      <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>참가비:</strong>"+program.getExpense() +"원" +"</li>";
    	msgg += "      <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>주제:</strong>"+ program.getSubject()+"</li>";
    	msgg += "      <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>입금 계좌:</strong><p style='color:blue'>하나은행 646-910019-24904 (천주교대전교구청소년교육원)</p></li>";
    	msgg += "    </ul>";
    	msgg += "    <div>" + program.getContent();
    	msgg += "    </div>";
    	msgg += "  </div>";
    	msgg += "</div>";
    	message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("hapum7179@gmail.com","<HAPUM>"));//보내는 사람
        return message;
    } 
    
    private MimeMessage rentalMessage(String to, Rental rental) throws Exception {
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, to); // 수신자 설정
        message.setSubject("대관 예약 정보 발송"); // 제목 설정
        // HTML 형식 메시지 내용 생성
        String msgg = "";
        msgg += "<div style='max-width:800px; margin:50px auto; padding:20px; border:1px solid #ddd; border-radius:12px; background-color:#f9f9f9;'>";
        msgg += "  <h2 style='color:#333;'>대관 예약 상세정보</h2>";
        msgg += "  <ul style='list-style:none; padding:0; font-size:16px;'>";
        msgg += "    <li style='margin-bottom:10px;'><strong>대관 날짜:</strong> " + rental.getReservationDate() + "</li>";
        msgg += "    <li style='margin-bottom:10px;'><strong>대관 장소:</strong> " + rental.getLocation() + "</li>";
        msgg += "    <li style='margin-bottom:10px;'><strong>대관 시작시간:</strong> " + rental.getStartTime() + "</li>";
        msgg += "    <li style='margin-bottom:10px;'><strong>대관 종료시간:</strong> " + rental.getEndTime() + "</li>";
        msgg += "    <li style='margin-bottom:10px;'><strong>대관료:</strong> " + rental.getPrice() + "원</li>";
        msgg += "    <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>입금 계좌:</strong><p style='color:blue'>하나은행 646-910019-24904 (천주교대전교구청소년교육원)</p></li>";
        msgg += "  </ul>";
        msgg += "</div>";

        // HTML 형식으로 본문 설정, 인코딩은 utf-8 사용
        message.setText(msgg, "utf-8", "html");
        message.setFrom(new InternetAddress("hapum7179@gmail.com", "<HAPUM>")); // 발신자 설정

        return message;
    }

    
    
    
    
    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random(System.currentTimeMillis());
 
        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤
 
            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return key.toString();
    }
    
    public String sendMessage(String to)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = createMessage(to);
        try{//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return ePw;
    }
    
    
    @Async
    public void sendProgramMessage(String to,Program program)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = programMessage(to, program);
        try{//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
    }
    @Async
    public void sendRentalMessage(String to,Rental rental)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = rentalMessage(to, rental);
        try{//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
    }
    @Async
    public CompletableFuture<String>  sendNewPassword(String to)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = newPassword(to);
        try{//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        String newPw = passwordEncoder.encode(ePw);
        
        return CompletableFuture.completedFuture(newPw);
    }
}
