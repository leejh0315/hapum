package hapum.hapum.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.VisitorLog;

@Mapper
public interface VisitorMapper {
	 void insertVisitorLog(VisitorLog visitorLog);
    
	 int countByDateTimeRange(
	            @Param("start") LocalDateTime start,
	            @Param("end") LocalDateTime end
	        );
	 int countAll();

}
