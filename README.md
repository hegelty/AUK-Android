https://github.com/hegelty/AUK-Backend
https://play.google.com/store/apps/details?id=com.unrevr.auk&pcampaignid=web_share

### Android 회귀 테스트

Android SDK 36과 Java 21을 설치한 뒤 `./gradlew :app:testDebugUnitTest`를 실행합니다. Robolectric에서 기존 데이터 이관과 알람 예약·복구, 퀴즈 화면 재생성을 검사합니다. 연결된 기기의 계측 테스트는 `./gradlew :app:connectedDebugAndroidTest`로 실행합니다.
