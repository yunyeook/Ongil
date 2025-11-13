package kr.co.ongil.global.util;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUtil {

    @Value("${file.upload.path:/uploads/}")
    private String basePath;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Template s3Template;

    public void deleteS3(String fileUrl) {
        try {
            // URL에서 S3 key 추출
            // https://bucket-name.s3.ap-northeast-2.amazonaws.com/profiles/uuid.jpg
            // -> profiles/uuid.jpg
            String key = extractKeyFromUrl(fileUrl);

            if (key != null && !key.isEmpty()) {
                s3Template.deleteObject(bucket, key);
                log.info("S3 파일 삭제 완료: {}", key);
            }
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            // 삭제 실패해도 메인 로직은 진행 (로그만 남김)
        }
    }

    public String saveS3(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // S3 키 생성
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString() + extension;
            String s3Key = subDirectory + "/" + fileName;

            // S3에 업로드
            s3Template.upload(bucket, s3Key, file.getInputStream());

            // URL 반환 (필요에 따라 선택)
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucket, "ap-northeast-2", s3Key);

            log.info("S3 파일 저장 완료: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("S3 파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    public String saveFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get(basePath, subDirectory);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String webPath = "/" + subDirectory + "/" + fileName;
            log.info("파일 저장 완료: {}", webPath);
            return webPath;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    public String saveProfileImage(MultipartFile file) {
        return saveS3(file, "profiles");
    }

    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        try {
            if (fileUrl.contains(".s3.")) {
                int keyStartIndex = fileUrl.indexOf(".amazonaws.com/");
                if (keyStartIndex > 0) {
                    return fileUrl.substring(keyStartIndex + ".amazonaws.com/".length());
                }
            }
            return null;
        } catch (Exception e) {
            log.error("URL에서 S3 key 추출 실패: {}", fileUrl);
            return null;
        }
    }

    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(basePath + filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("파일 삭제 완료: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            return false;
        }
    }
}