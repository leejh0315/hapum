package hapum.hapum.service;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.domain.News;
import hapum.hapum.mapper.NewsMapper;

@Service
public class NewsService{

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private NewsMapper newsMapper;


    
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
                photo.transferTo(destFile);
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
}

