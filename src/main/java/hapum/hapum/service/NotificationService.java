package hapum.hapum.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hapum.hapum.domain.Notification;
import hapum.hapum.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	
	private final NotificationMapper notificationMapper;
	
	public void insertNotification(Notification notification) {
		notificationMapper.insertNotification(notification);
		
	}
	public List<Notification> selectAll(){
		return notificationMapper.selectAll();
	}
	
	public Notification selectById(Long id) {
		return notificationMapper.selectById(id);	}

	public void updateOrder(List<Notification> notification) {
		for(Notification n : notification) {
			notificationMapper.updateOrder(n);
		}
	}
	
	public void toggleTop(Notification notification) {
		notificationMapper.toggleTop(notification);
	}
	
	public void toggleDown(Notification notification) {
		notificationMapper.toggleDown(notification);
	}
}
