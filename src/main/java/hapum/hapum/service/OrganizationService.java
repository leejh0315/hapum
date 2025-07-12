package hapum.hapum.service;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.domain.News;
import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationPost;
import hapum.hapum.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    
	private final OrganizationMapper organizationMapper;
	
	
	public List<Organization> selectAllOrganization(){
		return organizationMapper.selectAllOrganization();
	}
	
	public Organization selectOrganizationById(Long id){
		return organizationMapper.selectOrganizationById(id);
	}
	
	public List<OrganizationPost> selectByOrgId(Long id, int page, int size){
		int offset = (page - 1) * size;
		return organizationMapper.selectByOrgId(id, offset, size);
	}
	
	public int getTotalOrgPost(Long id) {
		return organizationMapper.countOrgPost(id);
	}
	
	public OrganizationPost selectOrgPostById(Long id) {
		return organizationMapper.selectOrgPostById(id);
	}
	
	public void deleteOrganization(Long orgId, String code) {
		organizationMapper.deleteOrganization(orgId, code);
	}
	
	public void deleteOrganizationPost(Long orgPostId) {
		organizationMapper.deleteOrganizationPost(orgPostId);
	}
	
	public List<Organization> selectAllOrganizationAdmin(){
		return organizationMapper.selectAllOrganizationAdmin();
	}
	
	public void insertOrganization(Organization organization, MultipartFile photo) {
        // 썸네일 사진이 있으면 업로드 처리
        if (photo != null && !photo.isEmpty()) {
            String originalFilename = photo.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;
            // 업로드할 대상 디렉토리: uploads/news/thumbnails/
            File targetDirectory = new File(uploadDir + "/organizationPost/");
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();  // 디렉토리 없으면 생성
            }
            File destFile = new File(targetDirectory, savedFilename);
            try {
                // 파일을 지정한 위치로 저장
                photo.transferTo(destFile);
                // News 객체의 썸네일 경로 업데이트 (웹 접근 경로)
                organization.setProfileSrc("/uploads/organizationPost/" + savedFilename);
            } catch (Exception e) {
                e.printStackTrace();
                // 필요한 경우 에러 처리 (예: 예외 던지기)
            }
        } else {
        	organization.setProfileSrc(null);
        }
        
        // 수정된 News 객체를 DB에 저장 (Repository 활용)
        organizationMapper.insertOrganization(organization);
    }
	
	public void updateOrganizationPost(OrganizationPost op, MultipartFile photo) {
		if (photo != null && !photo.isEmpty()) {
            String originalFilename = photo.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;
            // 업로드할 대상 디렉토리: uploads/news/thumbnails/
            File targetDirectory = new File(uploadDir + "/organizationPost/");
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();  // 디렉토리 없으면 생성
            }
            File destFile = new File(targetDirectory, savedFilename);
            try {
                // 파일을 지정한 위치로 저장
                photo.transferTo(destFile);
                // News 객체의 썸네일 경로 업데이트 (웹 접근 경로)
                op.setThumbnailSrc("/uploads/organizationPost/" + savedFilename);
                
                
            } catch (Exception e) {
                e.printStackTrace();
                // 필요한 경우 에러 처리 (예: 예외 던지기)
            }
        }
		organizationMapper.updateOrganizationPost(op);
	}
	
	public void updateOrganization(Organization organization, MultipartFile photo) {
		  if (photo != null && !photo.isEmpty()) {
	            String originalFilename = photo.getOriginalFilename();
	            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
	            String savedFilename = UUID.randomUUID().toString() + extension;
	            // 업로드할 대상 디렉토리: uploads/news/thumbnails/
	            File targetDirectory = new File(uploadDir + "/organizationPost/");
	            if (!targetDirectory.exists()) {
	                targetDirectory.mkdirs();  // 디렉토리 없으면 생성
	            }
	            File destFile = new File(targetDirectory, savedFilename);
	            try {
	                // 파일을 지정한 위치로 저장
	                photo.transferTo(destFile);
	                // News 객체의 썸네일 경로 업데이트 (웹 접근 경로)
	                organization.setProfileSrc("/uploads/organizationPost/" + savedFilename);
	            } catch (Exception e) {
	                e.printStackTrace();
	                // 필요한 경우 에러 처리 (예: 예외 던지기)
	            }
	        } else {
	        	organization.setProfileSrc(null);
	        }
		  
		  organizationMapper.updateOrganization(organization);
	}
	
	
	public void insertOrganizationWrite(OrganizationPost organizationPost, MultipartFile photo) {
        // 썸네일 사진이 있으면 업로드 처리
        if (photo != null && !photo.isEmpty()) {
            String originalFilename = photo.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;
            // 업로드할 대상 디렉토리: uploads/news/thumbnails/
            File targetDirectory = new File(uploadDir + "/organization/");
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();  // 디렉토리 없으면 생성
            }
            File destFile = new File(targetDirectory, savedFilename);
            try {
                // 파일을 지정한 위치로 저장
                photo.transferTo(destFile);
                // News 객체의 썸네일 경로 업데이트 (웹 접근 경로)
                organizationPost.setThumbnailSrc("/uploads/organization/" + savedFilename);
            } catch (Exception e) {
                e.printStackTrace();
                // 필요한 경우 에러 처리 (예: 예외 던지기)
            }
        } else {
        	organizationPost.setThumbnailSrc(null);
        }
        
        // 수정된 News 객체를 DB에 저장 (Repository 활용)
        organizationMapper.insertOrganizationWrite(organizationPost);
    }
	@Transactional
	public void deleteOrganizationAndPosts(Long orgId) {
		organizationMapper.deletePosts(orgId);
		organizationMapper.deleteOrganizationAndPosts(orgId);
	}
	
}
