package org.bang.testkube;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class UserAppConfiguration {

    @Bean
    public WebClient webClient() {
        // 1. 타임아웃 설정 (실무에서 매우 중요)
        // ClusterIP 통신은 빠르지만, Pod가 죽었을 때 무한 대기를 막기 위해 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                // 연결 타임아웃 5초
                .responseTimeout(Duration.ofSeconds(5))
                // 읽기 타임아웃 10초
                .responseTimeout(Duration.ofSeconds(10));

        // 2. WebClient 생성
        return WebClient.builder()
                // Base URL을 설정하지 않고, Controller에서 Full URI를 구성합니다.
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}