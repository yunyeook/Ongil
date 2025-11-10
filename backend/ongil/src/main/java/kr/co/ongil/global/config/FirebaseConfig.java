package kr.co.ongil.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        // 비동기 실행으로 WebSocket 등 다른 초기화와 충돌 방지
        CompletableFuture.runAsync(() -> {
            try (InputStream serviceAccount =
                new ClassPathResource("firebase/ongil-firebase-adminsdk-key.json").getInputStream()) {

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    // timeout 명시적으로 지정 (gRPC 리소스 충돌 방지)
                    .setConnectTimeout(5000)
                    .setReadTimeout(5000)
                    .build();

                boolean alreadyInitialized = FirebaseApp.getApps().stream()
                    .anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME));

                if (!alreadyInitialized) {
                    FirebaseApp.initializeApp(options);
                    log.info("✅ FirebaseApp 비동기 초기화 완료");
                } else {
                    log.info("ℹ️ FirebaseApp 이미 초기화됨");
                }

            } catch (Exception e) {
                log.error("❌ Firebase 초기화 실패", e);
            }
        });
    }
}
