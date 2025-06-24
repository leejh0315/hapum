package hapum.hapum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.Notification;

public interface NotificationMapper {

	void insertNotification(Notification notification);
	List<Notification> selectAll();
	Notification selectById(@Param("id")Long id);
}
