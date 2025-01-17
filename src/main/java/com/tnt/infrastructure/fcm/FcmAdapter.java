package com.tnt.infrastructure.fcm;

import static com.tnt.global.error.model.ErrorMessage.*;

import org.springframework.stereotype.Component;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.tnt.global.error.exception.TnTException;

@Component
public class FcmAdapter {

	public void sendNotificationByToken(String token, String title, String body, String clickActionUrl) {
		Message message = Message.builder()
			.setToken(token)
			.setNotification(com.google.firebase.messaging.Notification.builder()
				.setTitle(title)
				.setBody(body)
				.build())
			.putData("click_action", clickActionUrl)
			.build();

		try {
			FirebaseMessaging.getInstance().send(message);
		} catch (FirebaseMessagingException e) {
			throw new TnTException(FCM_FAILED, e);
		}
	}
}
