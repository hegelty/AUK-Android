# AUK Android 업데이트

기존 Google Play 항목은 `com.unrevr.auk`입니다. 이 소스의 업데이트 버전은 1.0.0 (24)이며 최소 API 28, 대상 API 36입니다.

## 배포 파일과 업데이트 확인

1. Play Console의 모든 트랙에서 가장 높은 버전 코드를 확인합니다. 24가 이미 사용되었으면 새 빌드 전에 `app/build.gradle`에서 올립니다.
2. `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:bundleRelease`를 실행합니다. 현재 소스에서 생성한 번들은 `app/build/outputs/bundle/release/app-release.aab`입니다. 저장소의 `app/release/app-release.aab`는 이전 버전입니다.
3. 기존 앱의 업로드 키로 번들에 서명하고 내부 테스트 트랙에 올립니다. Play에서 설치한 이전 버전으로 반복 알람과 즐겨찾기, 오답을 만든 뒤 동일 기기에서 업데이트하여 보존 여부를 확인합니다.
4. 정시 알람·알림 허용과 거절, 전체 화면 알림이 꺼진 상태, 오프라인 시작, 화면 회전, 잠금·재부팅 시 울림을 점검합니다. 같은 분에 예약한 알람과 채점 직후 앱 프로세스가 다시 시작된 경우도 확인합니다. 실제 기기에서는 소리·진동, 화면 켜짐, 다음 회차 발화 시각을 확인합니다.

## Play Console 신고 자료

| 기능 | 소스 기준 동작 | 운영자가 확인할 항목 |
| --- | --- | --- |
| 알람 | 사용자 지정 시각의 시계 알람을 예약합니다. 울리는 동안 `systemExempted` 유형의 포그라운드 서비스가 소리·진동을 처리합니다. 잠금화면 문제 표시에 전체 화면 알림을 사용합니다. | 앱 콘텐츠의 서비스 유형과 전체 화면 인텐트 신고에 실제 사용 흐름을 기재합니다. |
| 선택적 학습 점수 전송 | 공유를 켜면 설치 식별자와 학습 점수를 AUK 서버에 HTTPS로 전송합니다. 서버는 수신 시각과 함께 저장합니다. 공유 기본값은 꺼짐입니다. | 데이터 보안 양식의 ‘기기 또는 기타 ID’와 ‘앱 활동’에 선택 수집 및 통계 목적을 검토합니다. 서버·프록시 로그, 보유 기간, 위탁·제3자 제공, 삭제 문의 처리는 운영 현황으로 확인합니다. |
| 알람·오답·칼럼 | 알람 설정 및 오답 내역은 앱 저장소에 기록합니다. 칼럼 조회는 서버에 칼럼 번호를 보냅니다. | 실제 공개 개인정보처리방침 URL에 저장소의 `privacy.html`과 같은 내용을 게시하고 로그인 없이 읽히는지 확인합니다. 동일 파일이 앱에 포함됩니다. |

[Play 대상 API 정책](https://support.google.com/googleplay/android-developer/answer/11926878?hl=ko) · [포그라운드 서비스와 전체 화면 인텐트](https://support.google.com/googleplay/android-developer/answer/13392821?hl=ko) · [Play 데이터 보안 양식](https://support.google.com/googleplay/android-developer/answer/10787469?hl=ko)
