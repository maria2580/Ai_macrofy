# **Ai_Macrofy**

**Ai_Macrofy**는 사용자의 요청에 따라 자동화된 작업을 수행하는 **Android 애플리케이션**입니다. 이 앱은 **접근성 서비스(Accessibility Service)**를 활용하여 **앱 내에서 동작을 자동으로 실행**하거나, 사용자 인터페이스(UI)를 분석하고 필요한 작업을 자동으로 처리합니다. 이를 통해 **반복적인 작업**을 줄이고, 효율적인 사용자 경험을 제공합니다.

## **기능**

- **자동화된 터치**: 화면의 특정 좌표를 터치하여 앱을 제어합니다.
- **스크롤 및 스와이프**: 화면에서 스크롤 또는 스와이프 동작을 자동으로 수행합니다.
- **텍스트 입력**: 텍스트를 자동으로 입력하여 양식이나 검색을 자동으로 처리합니다.
- **동작 완료 여부 판단**: 사용자의 목표가 달성되었는지 분석하고, 필요 없는 동작을 반복하지 않도록 합니다.
- **유연한 작업 흐름**: 다양한 화면 레이아웃에 대응할 수 있도록 설계되었습니다.

## **프로젝트 구조**

- **`MainActivity`**: 앱의 메인 UI를 담당하는 Activity입니다.
- **`MacroAccessibilityService`**: 접근성 서비스를 사용하여 자동화된 작업을 실행하는 서비스입니다.
- **`LayoutAccessibilityService`**: 화면 레이아웃을 분석하여 필요한 작업을 판단하는 서비스입니다.
- **`MyForegroundService`**: 앱이 백그라운드에서도 지속적으로 작업을 수행할 수 있도록 도와주는 포그라운드 서비스입니다.

## **설치 방법**

1. **프로젝트 클론**
    
    이 프로젝트를 로컬 환경에 클론하려면 아래 명령어를 사용하세요:
    
    ```bash
    git clone https://github.com/yourusername/Ai_Macrofy.git
    
    ```
    
2. **Android Studio에서 프로젝트 열기**
    - Android Studio를 열고 `Ai_Macrofy` 프로젝트 폴더를 선택하여 엽니다.
3. **필요한 권한 설정**
    
    앱을 실행하려면 **접근성 서비스**와 **권한 요청**을 허용해야 합니다. 앱 설정에서 `Ai_Macrofy`를 선택하여 서비스를 활성화해주세요.
    
4. **빌드 및 실행**
    
    Android Studio에서 프로젝트를 빌드하고, 안드로이드 기기나 에뮬레이터에서 실행합니다.
    

## **사용 방법**

1. **앱 실행**: 앱을 실행하고 **인스타그램 실행** 또는 **다른 앱 실행**을 요청하는 텍스트를 입력합니다.
2. **작업 자동화**: 앱은 사용자가 설정한 작업을 자동으로 처리하고, 결과를 화면에 표시합니다.
3. **작업 완료 판단**: 앱은 자동으로 목표가 달성되었는지 판단하고, 완료된 작업에 대해 "done"을 반환합니다.
4. **스크롤 및 터치 작업**: 앱은 화면을 분석하여 자동으로 터치, 스크롤, 입력 등을 수행합니다.

## **주요 기능**

### **1. 자동화된 터치**

앱은 화면의 특정 좌표를 터치하여 동작을 실행합니다. 예를 들어, Instagram 앱을 실행하고 화면에서 특정 버튼을 자동으로 클릭합니다.

```json
{"actions":[{"type":"touch","coordinates":{"x":720,"y":794}}]}

```

### **2. 동작 완료 판단**

앱은 작업을 수행한 후, 목표가 달성되었는지 판단합니다. 목표가 달성되면 **`"done"`** 상태를 반환합니다.

```json
{"actions":[{"type":"done"}]}

```

### **3. 화면 레이아웃 분석**

앱은 UI의 상태를 분석하여, 이전과 동일한 화면인 경우 불필요한 동작을 반복하지 않도록 처리합니다.

## **필수 권한**

- **BIND_ACCESSIBILITY_SERVICE**: 접근성 서비스 사용 권한
- **INTERNET**: 외부 API 통신을 위한 인터넷 권한
- **SYSTEM_ALERT_WINDOW**: 화면에 오버레이 표시 기능
- **FOREGROUND_SERVICE**: 백그라운드 작업을 위한 권한

## **향후 계획**

- **AI 기반 UI 자동 분석**: 더 복잡한 UI 변화에 대한 동작 예측을 개선하여 다양한 화면을 처리할 수 있도록 개선합니다.
- **다양한 앱 지원**: Instagram 외에도 다른 인기 앱들에 대한 자동화 기능을 추가할 예정입니다.
- **UI 인터페이스 개선**: 사용자가 설정을 쉽게 변경할 수 있도록 UI를 개선할 예정입니다.

## **기여 방법**

1. 프로젝트를 **포크(fork)** 합니다.
2. 변경 사항을 **브랜치**에서 작업합니다.
3. 작업이 완료되면 **풀 리퀘스트(PR)**를 생성합니다.

## **라이선스**

이 프로젝트는 MIT 라이선스 하에 제공됩니다.
