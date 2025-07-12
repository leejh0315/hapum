package hapum.hapum.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramAdd;
import hapum.hapum.domain.ProgramSub;
import hapum.hapum.domain.ProgramWithSub;
import hapum.hapum.mapper.ProgramMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramService {

	@Value("${file.upload-dir}")
	private String uploadDir;

	private final ProgramMapper programMapper;

	public void insertProgram(Program program, MultipartFile imageFile) throws IOException {

		if (imageFile != null && !imageFile.isEmpty()) {
			// 확장자
			String originalFilename = imageFile.getOriginalFilename();
			String ext = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				ext = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			// UUID로 유니크 파일명 생성
			String uniqueFileName = UUID.randomUUID().toString() + ext;

			// 저장 경로 생성
			Path filePath = Paths.get(uploadDir + "/program/", uniqueFileName);
			Files.createDirectories(filePath.getParent());

			// 실제 파일 저장
			imageFile.transferTo(filePath.toFile());

			// DB에 저장할 경로 설정 (웹에서 접근 가능한 상대경로)
			String dbFilePath = "/uploads/program/" + uniqueFileName;
			program.setThumbnailSrc(dbFilePath);
		}
		if (program.getNeedOrgName() == "" || program.getNeedOrgName() == null) {
			program.setNeedOrgName("N");
		}
		if (program.getNeedPartCount() == "" || program.getNeedPartCount() == null) {
			program.setNeedPartCount("N");
		}
		if (program.getNeedRelation() == "" || program.getNeedRelation() == null) {
			program.setNeedRelation("N");
		}

		programMapper.insertProgram(program);
	}
	
	
	public void updateProgram(Program program, MultipartFile imageFile) throws IOException {
		if (imageFile != null && !imageFile.isEmpty()) {
			// 확장자
			String originalFilename = imageFile.getOriginalFilename();
			String ext = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				ext = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			// UUID로 유니크 파일명 생성
			String uniqueFileName = UUID.randomUUID().toString() + ext;

			// 저장 경로 생성
			Path filePath = Paths.get(uploadDir + "/program/", uniqueFileName);
			Files.createDirectories(filePath.getParent());

			// 실제 파일 저장
			imageFile.transferTo(filePath.toFile());

			// DB에 저장할 경로 설정 (웹에서 접근 가능한 상대경로)
			String dbFilePath = "/uploads/program/" + uniqueFileName;
			program.setThumbnailSrc(dbFilePath);
			}
		programMapper.updateProgram(program);
	}
	
	
	

	public void insertProgramAdd(ProgramAdd programadd, MultipartFile imageFile) throws IOException {

		if (imageFile != null && !imageFile.isEmpty()) {
			// 확장자
			String originalFilename = imageFile.getOriginalFilename();
			String ext = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				ext = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			// UUID로 유니크 파일명 생성
			String uniqueFileName = UUID.randomUUID().toString() + ext;

			// 저장 경로 생성
			Path filePath = Paths.get(uploadDir + "/program/thumbnail/", uniqueFileName);
			Files.createDirectories(filePath.getParent());

			// 실제 파일 저장
			imageFile.transferTo(filePath.toFile());

			// DB에 저장할 경로 설정 (웹에서 접근 가능한 상대경로)
			String dbFilePath = "/uploads/program/thumbnail/" + uniqueFileName;
			programadd.setThumbnailSrc(dbFilePath);
		}

		programMapper.insertProgramAdd(programadd);
	}
	
	
	public void updateProgramAdd(ProgramAdd programadd, MultipartFile imageFile) throws IOException {
		if (imageFile != null && !imageFile.isEmpty()) {
			// 확장자
			String originalFilename = imageFile.getOriginalFilename();
			String ext = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				ext = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			// UUID로 유니크 파일명 생성
			String uniqueFileName = UUID.randomUUID().toString() + ext;

			// 저장 경로 생성
			Path filePath = Paths.get(uploadDir + "/program/thumbnail/", uniqueFileName);
			Files.createDirectories(filePath.getParent());

			// 실제 파일 저장
			imageFile.transferTo(filePath.toFile());

			// DB에 저장할 경로 설정 (웹에서 접근 가능한 상대경로)
			String dbFilePath = "/uploads/program/thumbnail/" + uniqueFileName;
			programadd.setThumbnailSrc(dbFilePath);
		}
		programMapper.updateProgramAdd(programadd);
	}
	

	public List<Program> selectAllPrograms() {
		return programMapper.selectAllPrograms();
	}

	public Program selectProgramById(Long id) {
		return programMapper.selectProgramById(id);
	}

	public int getApplyCount(Long programId) {
		Integer applyCount = programMapper.getApplyCount(programId); // 신청자 수

		if (applyCount == null) {
			applyCount = 0;
		}
		return applyCount;
	}

	public int programSub(ProgramSub ps) {
		if (ps.getPartCount() == 0) {
			ps.setPartCount(1);
		}

		int temp = programMapper.programSub(ps);

		return temp;
	}

	public List<ProgramSub> selectPrograSubmById(Long ProgramId) {
		return programMapper.selectPrograSubmById(ProgramId);
	}

	public List<ProgramSub> getSubsWithUsers(Long programId) {
		return programMapper.selectByProgramIdWithUser(programId);
	}

	public void approve(Long psId) {
		programMapper.approve(psId);
	}

	public void cancel(Long psId) {
		programMapper.cancel(psId);
	}

	public List<ProgramWithSub> selectProgramByUserId(Long userId) {
		return programMapper.selectProgramsWithSubByUserId(userId);
	}

	public void deleteByProgramSubsId(Long subsId) {
		programMapper.deleteByProgramSubsId(subsId);
	}

	public void updateCode(Long programId, String code) {
		programMapper.updateCode(code, programId);
	}

	public List<ProgramAdd> selectAllProgramAdd() {
		return programMapper.selectAllProgramAdd();
	}

	public List<Program> selectProgramByAddId(Long id) {
		return programMapper.selectProgramByAddId(id);
	}

	public List<Program> selectThisMonthProgram() {
		return programMapper.selectThisMonthProgram();
	}

	public List<Program> getProgramsForWeek(int weekOffset) {
		LocalDate today = LocalDate.now();
		LocalDate startOfWeek = today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
		LocalDate endOfWeek = startOfWeek.plusDays(6);

		LocalDateTime startDateTime = startOfWeek.atStartOfDay();
		LocalDateTime endDateTime = endOfWeek.atTime(LocalTime.MAX);

		return programMapper.selectProgramsByDateRange(startDateTime, endDateTime);
	}
	
	
	public void updateProgramAddStatus(Long addId, String code) {
		programMapper.updateProgramAddStatus(addId, code);
	}
	
	public ProgramAdd selectProgramAddById(Long addId) {
		return programMapper.selectProgramAddById(addId);
	}
	
	@Transactional
	public void deleteProgram(Long id) {
		programMapper.deleteProgramSubs(id);
		programMapper.deleteProgram(id);
	}
	@Transactional
	public void deleteProgramAdd(Long id) {
		deleteProgram(id);
		programMapper.deleteProgramAdd(id);
	}
	

}
