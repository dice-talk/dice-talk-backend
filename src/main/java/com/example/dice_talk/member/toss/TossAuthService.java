package com.example.dice_talk.member.toss;

import im.toss.cert.sdk.TossCertSession; // Toss Cert Java SDK import
import im.toss.cert.sdk.TossCertSessionGenerator; // Toss Cert Java SDK import
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Toss 인증 API와 통신하여 AccessToken 발급, 인증 요청, 결과 조회 및 개인정보 복호화를 처리하는 서비스 클래스입니다.
 */
@Service
public class TossAuthService {

    // Toss 인증 API 테스트용 클라이언트 ID 및 시크릿 (실제 운영 시에는 외부 설정 파일에서 관리)
    private final String CLIENT_ID = "test_a8e23336d673ca70922b485fe806eb2d";
    private final String CLIENT_SECRET = "test_418087247d66da09fda1964dc4734e453c7cf66a7a9e3";

    // TossCertSessionGenerator는 서비스 초기화 시 한 번만 생성하여 재사용합니다.
    // 이 객체는 세션키 생성 및 암복호화에 사용될 세션 객체(TossCertSession)를 생성하는 역할을 합니다.
    private final TossCertSessionGenerator tossCertSessionGenerator;

    /**
     * TossAuthService 생성자입니다.
     * TossCertSessionGenerator를 초기화합니다.
     * 테스트 환경에서는 기본 생성자로 충분할 수 있으며, SDK가 내부적으로 테스트 설정을 사용합니다.
     * 운영 환경에서는 Toss로부터 발급받은 공개키를 사용하여 초기화해야 할 수 있습니다 (SDK 문서 참조).
     */
    public TossAuthService() {
        this.tossCertSessionGenerator = new TossCertSessionGenerator();
    }

    /**
     * Toss 인증 서버로부터 Access Token을 발급받습니다.
     * 발급받은 Access Token은 다른 Toss API 호출 시 인증 용도로 사용됩니다.
     * @return 발급받은 Access Token 문자열
     */
    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://oauth2.cert.toss.im/token"; // Access Token 발급 API EndPoint

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 요청 본문 형식 설정

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials"); // OAuth2 클라이언트 자격증명 그랜트 타입
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("scope", "ca"); // 인증기관(Certificate Authority) 관련 scope

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        // 예외 처리 또는 기본값 반환 로직 추가 필요
        throw new RuntimeException("Toss Access Token 발급에 실패했습니다.");
    }

    /**
     * Toss 인증 서버에 사용자의 본인확인 결과를 조회하고, 암호화된 개인정보를 복호화합니다.
     * @param accessToken Toss API 접근을 위한 Access Token
     * @param txId 결과 조회를 위한 트랜잭션 ID
     * @return 복호화된 개인정보를 포함할 수 있는 응답 결과 Map 객체
     * @throws Exception 복호화 과정 등에서 예외 발생 가능
     */
    public Map<String, Object> getVerificationResult(String accessToken, String txId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        // Toss 본인확인 결과조회 API EndPoint (정확한 URL은 Toss 최신 문서 확인 필요)
        String url = "https://cert.toss.im/api/v2/sign/user/auth/id/result";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Bearer 토큰 인증
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. 결과조회 API 호출 전에 SDK를 사용하여 새로운 세션(TossCertSession)을 생성합니다.
        // 이 세션 객체는 sessionKey 생성 및 추후 응답 데이터 복호화에 사용됩니다.
        TossCertSession sessionForVerification = this.tossCertSessionGenerator.generate();
        String sessionKeyForResult = sessionForVerification.getSessionKey();

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("txId", txId);
        requestBodyMap.put("sessionKey", sessionKeyForResult); // Toss 문서에 따라 결과조회 시 sessionKey 필수 전달

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBodyMap, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map<String, Object> responseBodyFromToss = response.getBody();
        System.out.println("Response body from Toss (raw - /auth/id/result): " + responseBodyFromToss);

        // 2. 응답이 성공적이고 personalData가 존재하면 복호화 처리
        if (responseBodyFromToss != null && "SUCCESS".equals(responseBodyFromToss.get("resultType"))) {
            Map<String, Object> successData = (Map<String, Object>) responseBodyFromToss.get("success");
            if (successData != null && successData.get("personalData") != null) {
                Map<String, Object> encryptedPersonalData = (Map<String, Object>) successData.get("personalData");
                Map<String, Object> decryptedPersonalData = new HashMap<>();

                // encryptedPersonalData의 각 필드 값을 복호화합니다.
                for (Map.Entry<String, Object> entry : encryptedPersonalData.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    System.out.println("Processing key: " + key + ", value: " + value); // 현재 값 확인 로그
                    // ✅ 수정된 조건문: "v1_"으로 시작하는지 확인 (또는 더 정확하게 "v1_0.0.14$" 등)
                    //    Toss SDK 버전(0.0.14)이 값에 포함되어 있으므로, 이를 고려해야 합니다.
                    //    단순히 "v1$"로 시작하는지 확인하면, "v1_..." 패턴을 놓칠 수 있습니다.
                    //    더 안전하게는 정규식을 사용하거나, SDK에서 암호화된 문자열인지 판별하는
                    //    메소드를 제공하는지 확인하는 것이 좋습니다.
                    //    일단은 "v1_"로 시작하고, "$_sessionId_$" 형태를 따르는지 정도로 체크해볼 수 있습니다.
                    //    Toss SDK가 암호화된 문자열에 특정 prefix (예: "v1_0.0.14$sessionId$encryptedData")를
                    //    붙이는 규칙이 있는지 확인하는 것이 가장 좋습니다.
                    //    여기서는 단순히 "v1_"로 시작하는 문자열을 암호화된 데이터로 간주합니다.
                    if (value instanceof String && ((String) value).startsWith("v1_")) {// 암호화된 데이터 형식인지 확인
                        System.out.println("Attempting to decrypt key: " + key); // 복호화 시도 로그
                        try {
                            String decryptedValue = sessionForVerification.decrypt((String) value);
                            decryptedPersonalData.put(key, decryptedValue);
                            System.out.println("Decrypted " + key + ": " + decryptedValue); // ✅ 복호화 성공 로그
                        } catch (Exception e) {
                            System.err.println("Failed to decrypt " + key + " (value: " + value + "): " + e.getMessage()); // ❗️ 복호화 실패 로그
                            e.printStackTrace(); // 상세 예외 스택 트레이스 출력
                            decryptedPersonalData.put(key, "Decryption Error: " + key); // 오류 발생 시 명확한 값으로 설정
                        }
                    } else {
                        decryptedPersonalData.put(key, value); // 암호화되지 않은 데이터(예: null)는 그대로 유지
                        System.out.println("Skipping decryption for key: " + key + " (not a v1$ string or not a string)");
                    }
                }
                successData.put("personalData", decryptedPersonalData); // 복호화된 (또는 오류 표시된) 데이터로 교체
            }
        }
        return responseBodyFromToss;
    }

    /**
     * Toss 본인인증을 시작하기 위한 요청을 생성하고, 인증 URL과 트랜잭션 ID(txId)를 반환합니다.
     * 현재 requestType은 USER_NONE (토스 표준창 또는 앱 호출 방식)을 기준으로 합니다.
     * @return 인증 URL(authUrl)과 트랜잭션 ID(txId)를 담은 Map 객체
     */
    public Map<String, String> createTossAuthRequest() {
        String accessToken = getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        // Toss 본인확인 요청 API EndPoint (정확한 URL은 Toss 최신 문서 확인 필요)
        String url = "https://cert.toss.im/api/v2/sign/user/auth/id/request";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("requestType", "USER_NONE"); // 사용자가 Toss 페이지/앱에서 직접 정보 입력
        body.put("successCallbackUrl", "dice-talk://toss-success"); // 인증 성공 시 돌아올 앱 딥링크
        body.put("failCallbackUrl", "dice-talk://toss-fail");     // 인증 실패 시 돌아올 앱 딥링크

        // USER_NONE 방식에서는 requestedInfo가 결과 조회 시 어떤 항목을 받고 싶은지를 명시합니다.
        // Toss 테스트 환경에서는 이 정보들을 가상 인물의 암호화된 데이터로 제공합니다.
        List<String> requestedInfo = Arrays.asList(
                "NAME",         // 이름
                "BIRTHDATE",    // 생년월일 (Toss API 문서에서 정확한 키워드 확인 필요)
                "GENDER",       // 성별
//                "PHONE", // 휴대폰 번호 (Toss API 문서에서 정확한 키워드 확인 필요)
                "CI"            // CI (연계정보)
        );
        body.put("requestedInfo", requestedInfo);
        System.out.println("Request body to Toss (createTossAuthRequest): " + body);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !"SUCCESS".equals(responseBody.get("resultType"))) {
            // 예외 처리 또는 오류 응답 로직 추가 필요
            System.err.println("Failed to create Toss auth request: " + responseBody);
            throw new RuntimeException("Toss 인증 요청 생성에 실패했습니다.");
        }

        Map<String, Object> success = (Map<String, Object>) responseBody.get("success");
        Map<String, String> result = new HashMap<>();

        // USER_NONE 방식의 응답에 따라 authUrl 또는 다른 값(appScheme 등)을 추출 (Toss 문서 확인)
        // 현재 프론트엔드(앱)에서 이 authUrl을 사용하여 Toss 인증창을 띄우거나 앱을 호출하게 됩니다.
        result.put("authUrl", (String) success.get("authUrl")); // 표준창 방식일 경우
        result.put("txId", (String) success.get("txId"));
        // 만약 앱스킴(appScheme, androidAppUri, iosAppUri)을 사용한다면 해당 필드도 추출 필요

        return result;
    }
}
