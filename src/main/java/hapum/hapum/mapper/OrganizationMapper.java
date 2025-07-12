package hapum.hapum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.Organization;
import hapum.hapum.domain.OrganizationPost;

@Mapper
public interface OrganizationMapper {

	List<Organization> selectAllOrganization();
	void insertOrganization(Organization organization);
	Organization selectOrganizationById(@Param("id")Long id);
	void insertOrganizationWrite(OrganizationPost organizationPost);
	List<OrganizationPost> selectByOrgId(@Param("orgId") Long id, @Param("offset") int offset, @Param("limit") int limit);
	int countOrgPost(@Param("orgId") Long id);
	OrganizationPost selectOrgPostById(@Param("id") Long id);
	void deleteOrganization(@Param("orgId") Long orgId, @Param("code") String code);
	void deleteOrganizationPost(@Param("orgPostId") Long orgPostId );
	List<Organization> selectAllOrganizationAdmin();
	void updateOrganization(Organization organization);
	void deletePosts(@Param("orgId") Long orgId);
	void deleteOrganizationAndPosts(@Param("orgId") Long orgId);
	void updateOrganizationPost(OrganizationPost organizationPost);
 }
