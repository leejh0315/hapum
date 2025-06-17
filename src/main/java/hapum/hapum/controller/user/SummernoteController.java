package hapum.hapum.controller.user;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SummernoteController {

    // application.properties에 설정된 파일 업로드 디렉토리
    @Value("${file.upload-dir}")
    private String uploadDir;

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
            InputStream fileStream = multipartFile.getInputStream();
            FileUtils.copyInputStreamToFile(fileStream, targetFile);
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
    
    public String moveTempImagesToPosts(String content, String type) {
    	
        // 정규표현식을 사용해 이미지 경로 추출 (예: src="/uploads/temp/파일명")
        Pattern pattern = Pattern.compile("src\\s*=\\s*\"(/uploads/temp/([^\"/]+))\"");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String tempImgPath = matcher.group(1);        // 예: /uploads/temp/uuid.jpg
            String fileName = matcher.group(2);             // 예: uuid.jpg
            File sourceFile = new File(uploadDir + "/temp/", fileName);
            // 매개변수 type에 맞춰 대상 폴더 경로 생성 (예: "/uploads/posts/" 또는 "/uploads/news/")
            File destFile = new File(uploadDir + "/" + type + "/", fileName);
            if (sourceFile.exists()) {
                try {
                    FileUtils.moveFile(sourceFile, destFile);
                    // content 내 이미지 경로도 temp 폴더 대신 해당 type 폴더로 변경
                    content = content.replace("/uploads/temp/", "/uploads/" + type + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                    // 이동 실패 시 추가 처리 (예: 로깅 또는 예외 발생)
                }
            }
        }
        return content;
    }

}
