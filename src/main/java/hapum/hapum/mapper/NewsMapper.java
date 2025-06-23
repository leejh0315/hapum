package hapum.hapum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.News;

@Mapper
public interface NewsMapper {
	public void insertNews(News news);
	List<News> selectAllNews(@Param("offset") int offset, @Param("limit") int limit);
	int countNews();
	News selectById(@Param("id")Long id);
	void updateNews(@Param("newsId") Long newsId, @Param("code") String code);
}
