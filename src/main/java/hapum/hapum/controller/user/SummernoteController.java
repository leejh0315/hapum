package hapum.hapum.controller.user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import net.coobird.thumbnailator.Thumbnails;
@Controller
public class SummernoteController {

    // application.properties에 설정된 파일 업로드 디렉토리
    @Value("${file.upload-dir}") private String uploadDir;
    @Value("${summernote.upload.temp-dir}") private String tempVideoDirStr;
    @Value("${summernote.upload.video-dir}") private String uploadVideoDirStr;
    private Path tempDir, videoDir;
    
    @PostConstruct
    public void init() throws IOException {
        tempDir = Paths.get(tempVideoDirStr);
        videoDir = Paths.get(uploadVideoDirStr);
        Files.createDirectories(Paths.get(uploadDir, "temp"));
        Files.createDirectories(Paths.get(uploadDir, "posts"));
        Files.createDirectories(tempDir);
        Files.createDirectories(videoDir);
    }
    
 // Summernote 이미지 업로드 (임시 저장)
    @RequestMapping(value = "/writePost/uploadSummernoteImageFile",
                    method = RequestMethod.POST,
                    produces = "application/json; charset=utf8")
    @ResponseBody
    public String uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile,
    										
                                            HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        String originalFileName = multipartFile.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;
        // 업로드 시 posts 대신 temp 폴더에 저장합니다.
        File targetFile = new File(uploadDir + "/temp/", savedFileName);

        try {
        	Thumbnails.of(multipartFile.getInputStream())
            .size(800, 800)         // ✅ 크기 제한 (최대 800px, 비율 유지)
            .outputQuality(0.7f)    // ✅ 품질 70%로 압축
            .toFile(targetFile); 
            // temp 폴더에 저장했으므로 반환 경로도 변경합니다.
            jsonObject.addProperty("src", "/uploads/temp/" + savedFileName);
            jsonObject.addProperty("responseCode", "success");
        } catch (IOException e) {
            FileUtils.deleteQuietly(targetFile);
            jsonObject.addProperty("responseCode", "error");
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    
    // Summernote 이미지 삭제 (삭제 요청 시 파일명을 전달받아 삭제)
    @RequestMapping(value = "/deleteSummernoteImageFile",
                    method = RequestMethod.POST,
                    produces = "application/json; charset=utf8")
    @ResponseBody
    public String deleteSummernoteImageFile(@RequestParam("file") String file) {
        // file 예: "/image/post/uuid.jpg" → 파일명만 추출
        String fileName = file.substring(file.lastIndexOf("/") + 1);
        // uploadDir를 사용하여 파일 경로 생성
        File targetFile = new File(uploadDir+"/posts/", fileName);
        boolean result = FileUtils.deleteQuietly(targetFile);

        JsonObject jsonObject = new JsonObject();
        if(result){
            jsonObject.addProperty("responseCode", "success");
        } else {
            jsonObject.addProperty("responseCode", "error");
        }
        return jsonObject.toString();
    }
    

    // ────────── 1) 에디터 임시 비디오 업로드 ──────────
    @PostMapping("/upload/video-temp")
    public ResponseEntity<Map<String, String>> uploadTemp(
            @RequestParam("file") MultipartFile file) throws IOException {

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = tempDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 에디터에서 src="/temp/videos/..." 로 접근하게 됨
        return ResponseEntity.ok(
                Collections.singletonMap("src", "/temp/videos/" + filename)
        );
    }

    // ────────── 2) 에디터에서 임시 비디오 삭제 요청 ──────────
    @PostMapping("/delete/temp/video")
    public ResponseEntity<Void> deleteTemp(@RequestParam("file") String fileUrl)
            throws IOException {

        String filename = Paths.get(fileUrl).getFileName().toString();
        Files.deleteIfExists(tempDir.resolve(filename));
        return ResponseEntity.ok().build();
    }



}
