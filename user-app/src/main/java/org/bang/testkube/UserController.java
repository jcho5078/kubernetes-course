package org.bang.testkube;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // 🚨 ClusterIP Service 이름은 변하지 않으므로 final로 선언
    private static final String PRODUCT_SERVICE_HOST = "localhost";

    private final WebClient webClient;

    // WebClient Bean을 생성자 주입
    public UserController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/")
    public String testUser(HttpServletRequest request
        , @Value("${spring.application.mode}") String mode
        , @Value("${spring.application.version}") String version) {

        // 1. 요청할 URI 구성 (ClusterIP Service 이름 사용)
        // Service 이름: product-app-service
        // Service Port: 80 (기본 HTTP 포트이므로 명시하지 않아도 됨. 만약 80이 아니면 :<Port>를 추가)
        String requestPath = "/product";
        String product = request.getParameter("product_id");

        // 2. 쿼리 파라미터 추가
        // WebClient는 URI Builder를 내부에 가지고 있어, RestTemplate처럼 복잡하게 URI 객체를 만들 필요가 없습니다.
        String result;
        String requestHost = request.getParameter("product_host");
        if(requestHost == null) requestHost = PRODUCT_SERVICE_HOST;

        // 다른 서비스에 통신할 url 설정 (기본 localhost)
        String requestUrl = "http://"+requestHost+"?product_id=" + product;

        log.info("Request Host: {}", requestUrl);

        try {
            // WebClient를 사용하여 논블로킹 방식으로 product-app에 요청
            // URI: http://product-app-service/product?product_id={product}
            result = webClient.get()
                    .uri(requestUrl)
                    .retrieve() // 응답을 가져옴
                    .bodyToMono(String.class) // 응답 본문을 String 타입으로 비동기 처리
                    .block(); // 🚨 단, Controller에서 동기적 결과를 위해 block() 사용*/

        } catch (Exception e) {
            log.error("Error calling Product Service ({}): {}", requestUrl, e.getMessage());
            // 통신 실패 시 대체 메시지 반환
            result = "Error retrieving product info.";
            // 🚨 실제 오류 진단을 위해 스택 트레이스 로그 (kubectl logs에서 확인)
            e.printStackTrace();
        }

        return "Hello : (" + mode + " : " + version + ") "  + request.getParameter("user_nm") + " : " + result;
    }
}
