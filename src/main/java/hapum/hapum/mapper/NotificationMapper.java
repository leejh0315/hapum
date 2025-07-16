package hapum.hapum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.Notification;

@Mapper
public interface NotificationMapper {

	void insertNotification(Notification notification);
	List<Notification> selectAll();
	Notification selectById(@Param("id")Long id);
	void updateOrder(Notification notification);
	void toggleTop(Notification notification);
	void toggleDown(Notification notification);
	void deleteByNotification(@Param("id") Long id);
	void updateNotification(Notification notification);
}
