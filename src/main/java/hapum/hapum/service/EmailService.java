package hapum.hapum.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

	@Value("${doc.program.output.dir}")
	private String outputDir;

	@Autowired
	JavaMailSender emailSender;

	private MimeMessage createMessage(String to) throws Exception {
		ePw = createKey();
		MimeMessage message = emailSender.createMimeMessage();

		message.addRecipients(RecipientType.TO, to);// 보내는 대상
		message.setSubject("비밀번호 발송");// 제목

		String msgg = "";
		msgg += "<div style='margin:20px;'>";
		msgg += "<h1> 안녕하세요 하품센터입니다. </h1>";
		msgg += "<br>";
		msgg += "<p>아래의 인증번호를 입력해주세요.<p>";
		msgg += "<br>";
		msgg += "<p>감사합니다.<p>";
		msgg += "<br>";
		msgg += "<div align='center' style='border:1px solid black; font-family:verdana; width: 500px'>";
		msgg += "<h3 style='color:blue;'>회원가입 인증번호 입니다.</h3>";
		msgg += "<div style='font-size:130%'>";
		msgg += "CODE : <strong>";
		msgg += ePw + "</strong><div><br/> ";
		msgg += "</div>";
		message.setText(msgg, "utf-8", "html");// 내용
		message.setFrom(new InternetAddress("hapum7179@gmail.com", "<HAPUM>"));// 보내는 사람
		redisUtils.setDataExpire(to, ePw, 180);
		return message;
	}

	private MimeMessage newPassword(String to) throws Exception {
		ePw = createKey();
		MimeMessage message = emailSender.createMimeMessage();
		message.addRecipients(RecipientType.TO, to);// 보내는 대상
		message.setSubject("임시 비밀번호 발송");// 제목

		String msgg = "";
		msgg += "<div style='margin:20px;'>";
		msgg += "<h1> 안녕하세요 하품센터입니다. </h1>";
		msgg += "<br>";
		msgg += "<p>아래의 임시 비밀번호로 로그인 후, 비밀번호 변경을 해주세요.<p>";
		msgg += "<br>";
		msgg += "<p>감사합니다.<p>";
		msgg += "<br>";
		msgg += "<div align='center' style='border:1px solid black; font-family:verdana; width: 500px'>";
		msgg += "<h3 style='color:blue;'>임시 비밀번호입니다.</h3>";
		msgg += "<div style='font-size:130%'>";
		msgg += "PASSWORD : <strong>";
		msgg += ePw + "</strong><div><br/> ";
		msgg += "</div>";
		message.setText(msgg, "utf-8", "html");// 내용
		message.setFrom(new InternetAddress("hapum7179@gmail.com", "<HAPUM>"));// 보내는 사람

		return message;
	}

	private MimeMessage programMessage(String to, Program program) throws Exception {

	    MimeMessage message = emailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

	    DateTimeFormatter dateFormatter =
	            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	    // =========================
	    // 1. 기본 메일 정보
	    // =========================
	    helper.setTo(to);
	    helper.setSubject("프로그램 정보 발송");
	    helper.setFrom(new InternetAddress("hapum7179@gmail.com", "<HAPUM>"));

	    // =========================
	    // 2. 값 미리 안전하게 꺼내기
	    // =========================
	    String title = program.getTitle() != null ? program.getTitle() : "-";
	    String location = program.getLocation() != null ? program.getLocation() : "-";
	    String target = program.getTarget() != null ? program.getTarget() : "-";
	    String subject = program.getSubject() != null ? program.getSubject() : "-";
	    String content = program.getContent() != null ? program.getContent() : "";
	    String expense = program.getExpense();               // null 가능
	    String needPartCount = program.getNeedPartCount();   // null 가능
	    String needOrgName = program.getNeedOrgName();       // null 가능
	    String thumbnailSrc = program.getThumbnailSrc();

	    // =========================
	    // 3. HTML 본문 구성
	    // =========================
	    StringBuilder html = new StringBuilder();

	    html.append("<div style='max-width:1000px;margin:50px auto;display:flex;flex-wrap:wrap;")
	        .append("gap:30px;padding:20px;border:1px solid #ddd;border-radius:12px;")
	        .append("background-color:#fffefa;'>");

	    // 썸네일
	    html.append("<div style='flex:1 1 300px;'>");
	    if (thumbnailSrc != null && !thumbnailSrc.isBlank()) {
	        html.append("<img src='cid:")
	            .append(thumbnailSrc)
	            .append("' style='width:100%;border-radius:12px;border:1px solid #ccc;'/>");
	    }
	    html.append("</div>");

	    // 내용 영역
	    html.append("<div style='flex:1 1 400px;display:flex;flex-direction:column;'>")
	        .append("<h2 style='margin-bottom:16px;color:#333;'>")
	        .append(title)
	        .append("</h2>")
	        .append("<ul style='list-style:none;padding:0;margin-bottom:20px;'>");

	    // 일시
	    if (program.getStartDate() != null && program.getEndDate() != null) {
	        html.append("<li><strong>일시:</strong> ")
	            .append(dateFormatter.format(program.getStartDate()))
	            .append(" ~ ")
	            .append(dateFormatter.format(program.getEndDate()))
	            .append("</li>");
	    }

	    // 장소 / 대상
	    html.append("<li><strong>장소:</strong> ").append(location).append("</li>")
	        .append("<li><strong>대상:</strong> ").append(target).append("</li>");

	    // =========================
	    // 4. 참가비
	    // =========================
	    if (expense != null && !expense.isBlank() && !"0".equals(expense)) {

	        html.append("<li><strong>1인 참가비:</strong> ")
	            .append(expense)
	            .append("원</li>")
	            .append("<li><strong>입금 계좌:</strong>")
	            .append("<p style='color:blue'>")
	            .append("하나은행 233-910013-52204 ((재)대전교구천주교유지재단 천안지역 청소년사목)")
	            .append("</p></li>");
	    }

	    // 주제
	    html.append("<li><strong>주제:</strong> ").append(subject).append("</li>");

	    // 소속 단체명
	    if (needOrgName != null && !needOrgName.isBlank()) {
	        html.append("<li><strong>소속 단체 명:</strong> ")
	            .append(needOrgName)
	            .append("</li>");
	    }

	    // =========================
	    // 5. 총 참가비 계산
	    // =========================
	    if (expense != null && needPartCount != null
	            && !"0".equals(expense)
	            && !"N".equals(needPartCount)
	            && expense.matches("[0-9,]+")
	            && needPartCount.matches("\\d+")) {

	        int expenseValue = Integer.parseInt(expense.replace(",", ""));
	        int countValue = Integer.parseInt(needPartCount);
	        int total = expenseValue * countValue;

	        DecimalFormat formatter2 = new DecimalFormat("###,###");

	        html.append("<li><strong>총 참가비:</strong> ")
	            .append(formatter2.format(total))
	            .append("원</li>");
	    }

	    html.append("</ul>")
	        .append("<div>")
	        .append(content)
	        .append("</div>")
	        .append("</div>")
	        .append("</div>");

	    helper.setText(html.toString(), true);

	    // =========================
	    // 6. 썸네일 inline 이미지
	    // =========================
	    if (thumbnailSrc != null && !thumbnailSrc.isBlank()) {
	        String realPath = thumbnailSrc.replaceFirst("^/uploads", "/upload");
	        FileSystemResource thumbnail =
	                new FileSystemResource(new File(realPath));

	        if (thumbnail.exists()) {
	            helper.addInline(thumbnailSrc, thumbnail);
	        }
	    }

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
		msgg += "    <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>입금 계좌:</strong><p style='color:blue'>하나은행 233-910013-52204((재)대전교구천주교유지재단 천안지역 청소년사목)</p></li>";
		msgg += "  </ul>";
		msgg += "</div>";

		// HTML 형식으로 본문 설정, 인코딩은 utf-8 사용
		message.setText(msgg, "utf-8", "html");
		message.setFrom(new InternetAddress("hapum7179@gmail.com", "<HAPUM>")); // 발신자 설정

		return message;
	}

	public MimeMessage programEmail(String to, String fileName) throws Exception {
		MimeMessage message = emailSender.createMimeMessage();

		// multipart=true 로 생성해야 첨부가 가능
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		// 1) 수신자/제목/본문/발신자 모두 helper로 설정
		helper.setTo(to);
		helper.setSubject("프로그램 신청서 발송");
		String html = "<h2 style=\"color:#333;\">프로그램 신청서</h2>";
		helper.setText(html, true);
		helper.setFrom("hapum7179@gmail.com", "HAPUM");

		// 2) 첨부파일 로딩
		File file = new File(outputDir, fileName);
		if (!file.exists() || !file.canRead()) {
			throw new FileNotFoundException("첨부파일을 찾을 수 없습니다: " + file.getAbsolutePath());
		}
		helper.addAttachment(file.getName(), new FileSystemResource(file));

		return message;
	}
	public MimeMessage rentalEmail(String to, String fileName) throws Exception {
		MimeMessage message = emailSender.createMimeMessage();

		// multipart=true 로 생성해야 첨부가 가능
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		// 1) 수신자/제목/본문/발신자 모두 helper로 설정
		helper.setTo(to);
		helper.setSubject("대관 신청서 발송");
		String html = "<h2 style=\"color:#333;\">프로그램 신청서</h2>";
		helper.setText(html, true);
		helper.setFrom("hapum7179@gmail.com", "HAPUM");

		// 2) 첨부파일 로딩
		File file = new File(outputDir, fileName);
		if (!file.exists() || !file.canRead()) {
			throw new FileNotFoundException("첨부파일을 찾을 수 없습니다: " + file.getAbsolutePath());
		}
		helper.addAttachment(file.getName(), new FileSystemResource(file));

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
				// a~z (ex. 1+97=98 => (char)98 = 'b')
				break;
			case 1:
				key.append((char) ((int) (rnd.nextInt(26)) + 65));
				// A~Z
				break;
			case 2:
				key.append((rnd.nextInt(10)));
				// 0~9
				break;
			}
		}
		return key.toString();
	}

	public String sendMessage(String to) throws Exception {
		// TODO Auto-generated method stub
		MimeMessage message = createMessage(to);
		try {// 예외처리
			emailSender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}
		return ePw;
	}

	@Async
	public void sendEmailProgram(String to, String fileName) throws Exception {
		MimeMessage message = programEmail(to, fileName);
		try {// 예외처리
			emailSender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}
	}
	@Async
	public void sendEmailRental(String to, String fileName) throws Exception {
		MimeMessage message = rentalEmail(to, fileName);
		try {// 예외처리
			emailSender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

	@Async
	public void sendProgramMessage(String to, Program program) throws Exception {
		// TODO Auto-generated method stub
		MimeMessage message = programMessage(to, program);
		try {// 예외처리
			emailSender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

	@Async
	public void sendRentalMessage(String to, Rental rental) throws Exception {
		// TODO Auto-generated method stub
		MimeMessage message = rentalMessage(to, rental);
		try {// 예외처리
			emailSender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}
	}
	
	@Async
	public CompletableFuture<String> sendNewPassword(String to) throws Exception {
		// TODO Auto-generated method stub
		MimeMessage message = newPassword(to);
		try {// 예외처리
			emailSender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}
		String newPw = passwordEncoder.encode(ePw);

		return CompletableFuture.completedFuture(newPw);
	}
}
