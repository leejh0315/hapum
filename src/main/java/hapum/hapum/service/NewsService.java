package hapum.hapum.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.domain.News;
import hapum.hapum.mapper.NewsMapper;
import net.coobird.thumbnailator.Thumbnails;

@Service
public class NewsService{

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private NewsMapper newsMapper;


    
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
    
    public void insertNews(News news, MultipartFile photo) {
        // 썸네일 사진이 있으면 업로드 처리
        if (photo != null && !photo.isEmpty()) {
            String originalFilename = photo.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;
            // 업로드할 대상 디렉토리: uploads/news/thumbnails/
            File targetDirectory = new File(uploadDir + "/news/");
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();  // 디렉토리 없으면 생성
            }
            File destFile = new File(targetDirectory, savedFilename);
            try {
                // 파일을 지정한 위치로 저장
            	Thumbnails.of(photo.getInputStream())
                .size(800, 800)              // 최대 크기 제한 (비율 유지)
                .outputQuality(0.7f)         // 품질을 70%로 낮춰 압축
                .toFile(destFile);   
            	
            	//photo.transferTo(destFile);
            	
            	
                // News 객체의 썸네일 경로 업데이트 (웹 접근 경로)
                news.setThumbnailSrc("/uploads/news/" + savedFilename);
            } catch (Exception e) {
                e.printStackTrace();
                // 필요한 경우 에러 처리 (예: 예외 던지기)
            }
        } else {
            news.setThumbnailSrc(null);
        }
        
        // 수정된 News 객체를 DB에 저장 (Repository 활용)
        newsMapper.insertNews(news);
    }
    public void updateNewsDetail(News news, MultipartFile photo) {
    	 if (photo != null && !photo.isEmpty()) {
             String originalFilename = photo.getOriginalFilename();
             String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
             String savedFilename = UUID.randomUUID().toString() + extension;
             // 업로드할 대상 디렉토리: uploads/news/thumbnails/
             File targetDirectory = new File(uploadDir + "/news/");
             if (!targetDirectory.exists()) {
                 targetDirectory.mkdirs();  // 디렉토리 없으면 생성
             }
             File destFile = new File(targetDirectory, savedFilename);
             try {
                 // 파일을 지정한 위치로 저장
            	 Thumbnails.of(photo.getInputStream())
                 .size(800, 800)              // 최대 크기 제한 (비율 유지)
                 .outputQuality(0.7f)         // 품질을 70%로 낮춰 압축
                 .toFile(destFile);   
             	
            	 
//                 photo.transferTo(destFile);
                 // News 객체의 썸네일 경로 업데이트 (웹 접근 경로)
                 news.setThumbnailSrc("/uploads/news/" + savedFilename);
             } catch (Exception e) {
                 e.printStackTrace();
                 // 필요한 경우 에러 처리 (예: 예외 던지기)
             }
         }
         newsMapper.updateNewsDetail(news);
    }
    
 
	public List<News> selectAllNews(int page, int size) {
		int offset = (page - 1) * size;
		return newsMapper.selectAllNews(offset, size);
	}
	
	public int getTotalNews() {
		return newsMapper.countNews();
	}
	
	public News selectById(Long id) {
		return newsMapper.selectById(id);
	}
	public void deleteNews(Long newsId, String code) {
		newsMapper.deleteNews(newsId);
	}
	
	public List<News> select20news() {
		return newsMapper.select20news();
	}

}

