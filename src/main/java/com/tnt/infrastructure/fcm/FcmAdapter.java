package com.tnt.infrastructure.fcm;

import static com.tnt.global.error.model.ErrorMessage.FCM_FAILED;

import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmAdapter {

	private final Environment environment;

	public void sendNotificationByToken(String token, String title, String body, Map<String, String> data) {
		String[] activeProfiles = environment.getActiveProfiles();
		boolean isLocal = activeProfiles.length > 0 && "local".equals(activeProfiles[0]);

		if (isLocal) {
			log.info("현재 local 환경이므로 FCM 메시지를 전송하지 않습니다. [title: {}, body: {}]", title, body);
			return;
		}

		Message message = Message.builder()
			.setToken(token)
			.setNotification(com.google.firebase.messaging.Notification.builder()
				.setTitle(title)
				.setBody(body)
				.build())
			.putAllData(data)
			.build();

		try {
			FirebaseMessaging.getInstance().send(message);
		} catch (FirebaseMessagingException e) {
			log.error(FCM_FAILED.getMessage(), e); // FCM 전송 실패해도 외부 로직은 그대로 수행
		}
	}
}
