package hapum.hapum.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
            Path filePath = Paths.get(uploadDir+"/program/", uniqueFileName);
            Files.createDirectories(filePath.getParent());

            // 실제 파일 저장
            imageFile.transferTo(filePath.toFile());

            // DB에 저장할 경로 설정 (웹에서 접근 가능한 상대경로)
            String dbFilePath = "/uploads/program/" + uniqueFileName;
            program.setThumbnailSrc(dbFilePath);
        }		
		
		
		programMapper.insertProgram(program);
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
            Path filePath = Paths.get(uploadDir+"/program/thumbnail/", uniqueFileName);
            Files.createDirectories(filePath.getParent());

            // 실제 파일 저장
            imageFile.transferTo(filePath.toFile());

            // DB에 저장할 경로 설정 (웹에서 접근 가능한 상대경로)
            String dbFilePath = "/uploads/program/thumbnail/" + uniqueFileName;
            programadd.setThumbnailSrc(dbFilePath);
        }		
		
		
		programMapper.insertProgramAdd(programadd);
	}
	
	
	
    public List<Program> selectAllPrograms() {
        return programMapper.selectAllPrograms();
    }
    
    public Program selectProgramById(Long id) {
    	return programMapper.selectProgramById(id);
    }
    
    
    public int getApplyCount(Long programId) {
    	int applyCount = programMapper.getApplyCount(programId);  // 신청자 수
    	return applyCount;
    }
    
    public int programSub(ProgramSub ps) {
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
    
    public List<ProgramWithSub> selectProgramByUserId(Long userId){
    	return programMapper.selectProgramsWithSubByUserId(userId);
    }
	public void deleteByProgramSubsId(Long subsId) {
		programMapper.deleteByProgramSubsId(subsId);
	}
	
	public void updateCode(Long programId, String code) {
		programMapper.updateCode(code ,programId);
	}
}
