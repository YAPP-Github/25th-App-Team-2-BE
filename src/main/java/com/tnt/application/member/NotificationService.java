package com.tnt.application.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tnt.infrastructure.fcm.FcmAdapter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final FcmAdapter fcmAdapter;

	public void sendConnectNotificationToTrainer(String fcmToken, String traineeName, Long trainerId, Long traineeId) {
		String title = "트레이니와 연결 완료";
		String body = traineeName + " 트레이니와 연결되었어요!";

		Map<String, String> data = new HashMap<>();
		data.put("trainerId", String.valueOf(trainerId));
		data.put("traineeId", String.valueOf(traineeId));

		fcmAdapter.sendNotificationByToken(fcmToken, title, body, data);
	}
}
