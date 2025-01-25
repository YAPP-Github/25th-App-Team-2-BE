package com.tnt.application.member;

import org.springframework.stereotype.Service;

import com.tnt.infrastructure.fcm.FcmAdapter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final FcmAdapter fcmAdapter;

	public void sendConnectNotificationToTrainer(String fcmToken, String traineeName) {
		String title = "트레이니와 연결 완료";
		String body = traineeName + " 트레이니와 연결되었어요!";

		fcmAdapter.sendNotificationByToken(fcmToken, title, body);
	}
}
