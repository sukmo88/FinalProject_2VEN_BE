package com.sysmatic2.finalbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 클라이언트가 웹 소켓에 연결할 엔드포인트 설정
    registry.addEndpoint("/ws")
            .setAllowedOrigins("*") // 필요에 따라 제한
            .withSockJS(); // SockJS 지원
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 메시지 브로커 설정
    registry.enableSimpleBroker("/topic"); // 브로커가 메시지를 브로드캐스트할 경로
    registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지를 보낼 때 사용하는 프리픽스
  }
}
