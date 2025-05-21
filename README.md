# Ai_macrofy - AI-Powered UI Automation Tool

## 🌟 소개

**Ai_macrofy**는 사용자의 자연어 명령을 이해하고, OpenAI의 GPT-4o-mini 모델을 활용하여 Android 장치의 UI(사용자 인터페이스) 작업을 자동으로 수행하는 혁신적인 애플리케이션입니다. 이 앱은 접근성 서비스를 통해 현재 화면의 레이아웃을 분석하고, AI가 생성한 명령에 따라 화면을 직접 조작하여 복잡한 작업을 자동화합니다.

예를 들어, "인스타그램을 열고 첫 번째 게시물에 좋아요를 눌러줘"와 같은 명령을 이해하고 실행할 수 있도록 설계되었습니다.

## ✨ 주요 기능

* **자연어 명령 이해 및 실행:** 사용자가 입력한 텍스트 명령을 GPT-4o-mini가 분석하여 UI 자동화 작업으로 변환합니다.
* **화면 레이아웃 분석:** `LayoutAccessibilityService`를 사용하여 현재 화면의 UI 요소(텍스트, 버튼, 좌표 등)를 JSON 형태로 추출합니다.
* **GPT 기반 의사결정:** 추출된 화면 레이아웃 정보와 사용자 명령, 그리고 이전 작업 이력을 바탕으로 GPT-4o-mini가 다음 수행할 작업을 결정합니다.
* **상황인지적 작업 수행:** 이전 화면 레이아웃의 해시값과 GPT의 이전 응답을 기억하여, 동일한 화면에서 불필요한 반복 작업을 방지하고 보다 지능적으로 작업을 이어갑니다.
* **자동화된 UI 상호작용:** `MacroAccessibilityService`를 통해 다양한 UI 조작(터치, 롱 터치, 스와이프, 스크롤, 텍스트 입력, 드래그 앤 드롭, 더블 탭) 및 시스템 전역 액션(뒤로 가기, 홈, 최근 앱)을 수행합니다.
* **백그라운드 실행:** `MyForegroundService`를 통해 앱이 화면에 보이지 않아도 매크로 작업을 백그라운드에서 안정적으로 실행합니다.
* **API 키 관리:** 사용자가 자신의 OpenAI API 키를 안전하게 저장하고 사용할 수 있도록 SharedPreferences를 활용합니다.

## 🚀 작동 방식 (워크플로우)

1.  **설정:** 사용자는 `MainActivity`에서 자신의 OpenAI API 키를 입력하고 저장합니다.
2.  **명령 입력:** 사용자는 수행하고자 하는 작업을 자연어로 입력합니다 (예: "카카오톡 열고 친구에게 메시지 보내줘").
3.  **매크로 시작:** 사용자가 "START MACRO" 버튼을 누르면 `MyForegroundService`가 시작됩니다.
4.  **주기적 작업 루프 (`MyForegroundService`):**
    a.  **화면 분석:** `LayoutAccessibilityService`가 현재 화면의 UI 요소 정보를 JSON 형태로 추출합니다.
    b.  **프롬프트 구성:** 추출된 화면 정보, 사용자의 최초 명령, 그리고 이전 (화면 해시, GPT 응답) 이력 쌍들이 포함된 상세한 프롬프트를 구성합니다. 이 프롬프트는 GPT가 다음 행동을 결정하고, 반복적인 행동을 피하며, 작업 완료를 인지하도록 유도합니다.
    c.  **GPT API 호출:** `GPTManager`를 통해 구성된 프롬프트를 OpenAI GPT-4o-mini 모델로 전송합니다.
    d.  **액션 지시 수신:** GPT는 수행할 다음 작업을 특정 JSON 형식(예: `{"actions":[{"type":"touch", "coordinates":{"x":100, "y":200}}]}`)으로 반환합니다.
    e.  **액션 실행:** `MacroAccessibilityService`가 이 JSON 응답을 파싱하여 화면에 해당 UI 조작을 실행합니다.
    f.  **반복:** 작업이 GPT에 의해 "done"으로 판단되거나 사용자가 "STOP MACRO" 버튼을 누를 때까지 위 과정(a-e)을 반복합니다 (기본 1초 간격).

## 📸 주요 화면
<table align="center">
      <tr>
            <td align="center">
                  <image src="https://github.com/user-attachments/assets/15d0057d-13b2-4bac-bf45-d9a88d6c0c2f" width="400rem">
            </td>
            <td align="center">
                  <image src="https://github.com/user-attachments/assets/75a8b9ac-f879-44fc-9250-83268922aab0" width="300rem">
                  <image src="https://github.com/user-attachments/assets/e3492429-2a3f-428b-90e5-39552562185e" width="300rem">
            </td>
      </tr>
      <tr>
            <td align="center">
                  메인 화면 (`MainActivity`)
            </td>
            <td align="center">
                  접근성 서비스 활성화 안내
            </td>
      </tr>
</table>
<hr>
   <div align="center">
       <video src="https://github.com/user-attachments/assets/a5ae8ea7-be13-46fb-a3f1-1f28f2085216" width="400">
   </div> 
   <div align="center">
      <p>open_ai의 프로젝트 api 키를 주입하고 명령을 타이핑하여 입력한다.</p>
  <p>실행버튼을 누르자 홈 화면으로 이동하여 크롬을 실행한다. 이 과정에서 사용자의 개입은 일어나지 않았다.</p>
   </div>

## 🛠️ 기술 스택

* **언어:** Kotlin
* **플랫폼:** Android
* **핵심 기술:**
    * Android Accessibility Services: 화면 정보 읽기 및 UI 조작
    * OpenAI GPT-4o-mini API: 자연어 이해 및 작업 지시 생성
* **네트워킹:** Retrofit2, OkHttp, Gson (OpenAI API 연동)
* **비동기 처리:** Kotlin Coroutines
* **서비스:** Android Foreground Service
* **데이터 저장:** SharedPreferences (API 키 저장)

## ⚙️ 설정 및 사용법

### 사전 준비 사항

1.  **Android 장치/에뮬레이터:** 안드로이드 운영체제.
2.  **OpenAI API Key:** OpenAI 플랫폼에서 발급받은 유효한 API 키.

### 빌드 및 실행

1.  프로젝트를 Android Studio에서 엽니다.
2.  필요한 경우 Gradle 동기화를 실행합니다.
3.  Android 장치 또는 에뮬레이터에 앱을 빌드하고 설치합니다.

### 초기 설정

1.  **접근성 서비스 활성화:**
    * 앱을 처음 실행하거나 매크로 시작 시, 접근성 서비스 권한이 필요하다는 안내가 나올 수 있습니다.
    * Android 설정 > 접근성 > 설치된 서비스 (또는 유사한 메뉴)로 이동하여 **"Ai_macrofy"** (또는 Manifest에 정의된 `LayoutAccessibilityService` 및 `MacroAccessibilityService`의 레이블) 두 가지 모두 활성화합니다.
2.  **OpenAI API 키 입력:**
    * 앱 메인 화면에서 발급받은 OpenAI API 키를 입력하고 "Save API Key" 버튼을 눌러 저장합니다.

### 매크로 실행

1.  메인 화면의 "Enter your prompt here..." 필드에 자동화하고 싶은 작업을 자연어로 입력합니다.
2.  "START MACRO" 버튼을 누릅니다.
3.  앱이 백그라운드에서 작업을 수행하며, 필요한 경우 화면 상단에 Foreground Service 알림이 표시됩니다.
4.  작업을 중단하고 싶으면 "STOP MACRO" 버튼을 누릅니다.

## 🔑 주요 컴포넌트

* **`MainActivity.kt`**: 사용자 인터페이스 제공, 프롬프트 및 API 키 입력, 매크로 시작/중지 제어.
* **`GPTManager.kt`**: OpenAI GPT API와의 모든 통신을 관리. 프롬프트 구성 및 응답 처리를 담당.
* **`LayoutAccessibilityService.kt`**: 현재 화면의 레이아웃 정보를 실시간으로 추출하여 JSON 형태로 제공.
* **`MacroAccessibilityService.kt`**: GPT로부터 받은 JSON 형식의 액션 지시를 실제 안드로이드 UI 조작으로 변환하여 실행.
* **`MyForegroundService.kt`**: 레이아웃 추출, GPT API 호출, 액션 실행 등의 매크로 핵심 로직을 백그라운드에서 조율하고 실행하는 포그라운드 서비스. GPT에게 이전 작업 이력(화면 해시, GPT 액션)을 전달하여 상황에 맞는 판단을 유도.
* **프롬프트 엔지니어링 (`MyForegroundService.sendToGPT`):** GPT가 정확한 JSON 형식의 액션을 반환하고, 반복적인 작업을 피하며, 작업 완료를 인지하도록 상세한 지침과 현재 화면 정보, 그리고 작업 이력을 포함한 프롬프트를 구성.

## 📋 권한 요구 사항

본 앱은 다음 권한을 필요로 합니다 (`AndroidManifest.xml` 참조):

* `android.permission.BIND_ACCESSIBILITY_SERVICE`: 접근성 서비스 사용을 위해 필수.
* `android.permission.INTERNET`: OpenAI API 통신을 위해 필요.
* `android.permission.FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC`: 백그라운드 매크로 실행을 위한 포그라운드 서비스에 필요.
* `android.permission.SYSTEM_ALERT_WINDOW`: (매니페스트에 선언됨) - 현재 코드에서는 직접적인 사용처가 명확하지 않으나, 특정 UI 오버레이 기능에 필요할 수 있습니다.
* `android.permission.CAMERA` (`required="false"`): (매니페스트에 선언됨) - 현재 코드에서는 사용되지 않는 것으로 보입니다.

## 📄 설정 파일

* **`accessibility_service_config.xml`** (프로젝트 내 `res/xml/` 폴더에 위치해야 함 - 현재 제공되지 않음): 접근성 서비스의 상세 설정을 정의합니다. (예: 감지할 이벤트 유형, 접근할 수 있는 패키지 이름 등). 이 파일이 올바르게 설정되어야 서비스가 정상 작동합니다.

## 💡 참고 및 제한 사항

* 자동화의 정확성과 성공률은 GPT 모델의 이해도, 화면 레이아웃의 복잡성 및 일관성, 프롬프트의 명확성에 따라 달라질 수 있습니다.
* `MyForegroundService`에서 화면 변경을 감지하기 위해 사용된 "레이아웃 해시" (`layoutInfo.toString().length`)는 단순화된 방식으로, 복잡한 UI 변경을 정확히 감지하기에는 한계가 있을 수 있습니다. (향후 개선 가능)
* 접근성 서비스는 강력한 권한을 가지므로, 신뢰할 수 있는 앱에서만 활성화해야 합니다.
* `MediaProjection` API를 사용한 화면 직접 캡처 기능은 현재 코드에서 주석 처리되어 있습니다. (이 기능을 사용하려면 추가 권한 및 구현 필요).
