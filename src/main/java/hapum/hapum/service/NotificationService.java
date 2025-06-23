package hapum.hapum.service;

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

}
