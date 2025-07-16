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
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		// 1) 받는 사람·제목·보내는 사람 설정
		helper.setTo(to);
		helper.setSubject("프로그램 정보 발송");
		helper.setFrom(new InternetAddress("hapum7179@gmail.com", "<HAPUM>"));

		// 2) HTML 본문 구성
		StringBuilder html = new StringBuilder();
		html.append("<div style='max-width:1000px;margin:50px auto;display:flex;flex-wrap:wrap;")
				.append("gap:30px;padding:20px;border:1px solid #ddd;border-radius:12px;")
				.append("background-color:#fffefa;'>").append("<div style='flex:1 1 300px;'>").append("<img src='cid:")
				.append(program.getThumbnailSrc()).append("' alt='프로그램 썸네일' style='width:100%;border-radius:12px;")
				.append("border:1px solid #ccc;'/>").append("</div>")
				.append("<div style='flex:1 1 400px;display:flex;flex-direction:column;")
				.append("justify-content:space-between;'>").append("<h2 style='margin-bottom:16px;color:#333;'>")
				.append(program.getTitle()).append("</h2>")
				.append("<ul style='list-style:none;padding:0;margin-bottom:20px;'>")
				.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
				.append("<strong>일시:</strong>");

		if (program.getStartDate() != null) {
			html.append(formatter.format(program.getStartDate())).append("~")
					.append(formatter.format(program.getEndDate()));
		}

		html.append("</li>").append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
				.append("<strong>장소:</strong>").append(program.getLocation()).append("</li>")
				.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
				.append("<strong>대상:</strong>").append(program.getTarget()).append("</li>");

		if (program.getExpense() != null || !program.getExpense().equals("")) {
			html.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
					.append("<strong>1인 참가비:</strong>").append(program.getExpense()).append("원").append("</li>")
					.append("    <li style='margin-bottom:10px;font-size:15px;line-height:1.4;'><strong>입금 계좌:</strong><p style='color:blue'>하나은행 646-910019-24904 (천주교대전교구청소년교육원)</p></li>");

		}

		html.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>").append("<strong>주제:</strong>")
				.append(program.getSubject()).append("</li>");

		if (program.getNeedOrgName() != null || !program.getNeedOrgName().equals("")) {
			html.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
					.append("<strong>소속 단체 명:</strong>").append(program.getNeedOrgName()).append("</li>");
		}

		if (!program.getExpense().equals("0") && !program.getNeedPartCount().equals("N")) {
			html.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
					.append("<strong>총 참가비:</strong>");

			// 콤마 제거 후 계산
			int expense = Integer.parseInt(program.getExpense().replace(",", ""));
			int count = Integer.parseInt(program.getNeedPartCount());
			int total = expense * count;

			// 천 단위 콤마 포맷
			DecimalFormat formatter2 = new DecimalFormat("###,###");
			String formattedTotal = formatter2.format(total);

			html.append(formattedTotal).append("원").append("</li>");
		}

		if (program.getNeedPartCount() != null || program.getNeedPartCount() != "") {
			html.append("<li style='margin-bottom:10px;font-size:15px;line-height:1.4;'>")
					.append("<strong>참가자와의 관계:</strong>").append(program.getNeedRelation()).append("</li>");
		}

		html.append("</ul>").append("<div>").append(program.getContent()).append("</div>").append("</div>")
				.append("</div>");

		helper.setText(html.toString(), true);

		// 3) 실제 파일 경로로 치환
		// (program.getThumbnailSrc() => "/uploads/program/xxx.png")
		String logicalPath = program.getThumbnailSrc();
		String realPath = logicalPath.replaceFirst("^/uploads", "/upload");

		FileSystemResource thumbnail = new FileSystemResource(new File(realPath));
		if (!thumbnail.exists()) {
			throw new IllegalArgumentException("썸네일 파일이 없습니다: " + realPath);
		}

		// 4) HTML <img>의 cid와 동일한 key 로 inline 이미지 등록
		helper.addInline(logicalPath, thumbnail);

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
