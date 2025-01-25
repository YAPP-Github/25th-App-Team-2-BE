package com.tnt.application.member;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tnt.infrastructure.fcm.FcmAdapter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private FcmAdapter fcmAdapter;

	@Test
	@DisplayName("트레이너와 연결 알림 전송 호출 성공")
	void sendConnectNotificationToTrainer_success() {
		// given
		String fcmToken = "32f23fa31t";
		String traineeName = "김영명";

		// when
		notificationService.sendConnectNotificationToTrainer(fcmToken, traineeName, 1L, 2L);

		// then
		verify(fcmAdapter, times(1)).sendNotificationByToken(anyString(), anyString(), anyString(), anyMap());
	}
}
