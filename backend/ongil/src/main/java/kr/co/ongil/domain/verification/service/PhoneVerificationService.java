package kr.co.ongil.domain.verification.service;

import kr.co.ongil.domain.verification.dto.request.SendVerificationRequest;
import kr.co.ongil.domain.verification.dto.request.VerifyCodeRequest;
import kr.co.ongil.domain.verification.dto.response.VerificationResponse;
import kr.co.ongil.domain.verification.entity.VerificationGrant;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.sms.SmsService;
import kr.co.ongil.global.util.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 전화번호 인증 서비스
 * - 인증번호 발송 (Redis 저장, SMS 전송)
 * - 인증번호 검증 및 1회용 토큰 발급
 * - Redis 기반 요청 제한 (전화번호당, IP당)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;

    // Redis Key Prefix
    private static final String CODE_KEY_PREFIX = "verify:code:";
    private static final String COUNT_KEY_PREFIX = "verify:count:";
    private static final String LAST_REQUEST_KEY_PREFIX = "verify:last:";
    private static final String IP_COUNT_KEY_PREFIX = "verify:ip:";
    private static final String ATTEMPT_KEY_PREFIX = "verify:attempt:";

    // 제한 정책
    private static final int MAX_REQUEST_PER_PHONE = 5; // 전화번호당 1시간 내 최대 요청 횟수
    private static final int MAX_REQUEST_PER_IP = 20;   // IP당 1시간 내 최대 요청 횟수
    private static final int MAX_VERIFY_ATTEMPTS = 5;   // 인증번호 입력 최대 시도 횟수
    private static final long CODE_TTL_SECONDS = 180;   // 인증번호 유효 시간 (3분)
    private static final long RATE_LIMIT_SECONDS = 60;  // 재요청 최소 간격 (1분)
    private static final long COUNT_TTL_SECONDS = 3600; // 요청 횟수 카운트 TTL (1시간)

    /**
     * 인증번호 발송
     *
     * @param request   전화번호 요청
     * @param ipAddress 요청자 IP
     */
    public void sendVerificationCode(SendVerificationRequest request, String ipAddress) {
        String phoneNumber = request.phoneNumber();

        log.info("인증번호 발송 요청: phoneNumber={}, ip={}", phoneNumber, ipAddress);

        // 1. IP 기준 요청 횟수 제한 체크
        checkIpRateLimit(ipAddress);

        // 2. 전화번호 기준 요청 횟수 제한 체크
        checkPhoneRateLimit(phoneNumber);

        // 3. 마지막 요청 시간 체크 (1분 간격 제한)
        checkLastRequestTime(phoneNumber);

        // 4. 인증번호 생성
        String code = VerificationCodeGenerator.generate();

        // 5. Redis에 저장 (TTL 3분)
        String codeKey = CODE_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(CODE_TTL_SECONDS));

        // 6. 요청 횟수 증가 (전화번호, IP)
        incrementRequestCount(phoneNumber, ipAddress);

        // 7. 마지막 요청 시간 기록
        updateLastRequestTime(phoneNumber);

        // 8. 인증 시도 횟수 초기화
        resetVerifyAttempts(phoneNumber);

        // 9. SMS 발송
        smsService.sendVerificationCode(phoneNumber, code);

        log.info("인증번호 발송 완료: phoneNumber={}", phoneNumber);
    }

    /**
     * 인증번호 검증 및 토큰 발급
     *
     * @param request 인증번호 검증 요청
     * @return 인증 성공 응답 (1회용 토큰 포함)
     */
    public VerificationResponse verifyCode(VerifyCodeRequest request) {
        String phoneNumber = request.phoneNumber();
        String inputCode = request.verificationCode();
        VerificationGrant grant = VerificationGrant.fromString(request.grants());

        log.info("인증번호 검증 요청: phoneNumber={}, grant={}", phoneNumber, grant);

        // 1. 인증 시도 횟수 체크
        checkVerifyAttempts(phoneNumber);

        // 2. Redis에서 인증번호 조회
        String codeKey = CODE_KEY_PREFIX + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.warn("인증번호 없음 또는 만료: phoneNumber={}", phoneNumber);
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        // 3. 인증번호 일치 여부 확인
        if (!storedCode.equals(inputCode)) {
            incrementVerifyAttempts(phoneNumber);
            log.warn("인증번호 불일치: phoneNumber={}, input={}", phoneNumber, inputCode);
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 4. 인증 성공 → Redis에서 인증번호 삭제
        redisTemplate.delete(codeKey);
        resetVerifyAttempts(phoneNumber);

        // 5. 1회용 목적제한 토큰 발급
        String verificationToken = jwtUtil.generateVerificationToken(phoneNumber, grant);

        log.info("인증 성공 및 토큰 발급 완료: phoneNumber={}, grant={}", phoneNumber, grant);

        return VerificationResponse.of(true, verificationToken);
    }

    // ========== Private 메서드: 제한 정책 검증 ==========

    /**
     * IP 기준 요청 횟수 제한 체크
     */
    private void checkIpRateLimit(String ipAddress) {
        String ipKey = IP_COUNT_KEY_PREFIX + ipAddress;
        String countStr = redisTemplate.opsForValue().get(ipKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= MAX_REQUEST_PER_IP) {
            log.warn("IP 요청 횟수 초과: ip={}, count={}", ipAddress, count);
            throw new BusinessException(ErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * 전화번호 기준 요청 횟수 제한 체크
     */
    private void checkPhoneRateLimit(String phoneNumber) {
        String countKey = COUNT_KEY_PREFIX + phoneNumber;
        String countStr = redisTemplate.opsForValue().get(countKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= MAX_REQUEST_PER_PHONE) {
            log.warn("전화번호 요청 횟수 초과: phoneNumber={}, count={}", phoneNumber, count);
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_LIMIT_EXCEEDED);
        }
    }

    /**
     * 마지막 요청 시간 체크 (1분 간격 제한)
     */
    private void checkLastRequestTime(String phoneNumber) {
        String lastKey = LAST_REQUEST_KEY_PREFIX + phoneNumber;
        String lastTimeStr = redisTemplate.opsForValue().get(lastKey);

        if (lastTimeStr != null) {
            long lastTime = Long.parseLong(lastTimeStr);
            long currentTime = System.currentTimeMillis();
            long elapsedSeconds = (currentTime - lastTime) / 1000;

            if (elapsedSeconds < RATE_LIMIT_SECONDS) {
                log.warn("재요청 간격 미달: phoneNumber={}, elapsed={}초", phoneNumber, elapsedSeconds);
                throw new BusinessException(ErrorCode.PHONE_VERIFICATION_RATE_LIMIT);
            }
        }
    }

    /**
     * 인증번호 입력 시도 횟수 체크
     */
    private void checkVerifyAttempts(String phoneNumber) {
        String attemptKey = ATTEMPT_KEY_PREFIX + phoneNumber;
        String attemptStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptStr != null ? Integer.parseInt(attemptStr) : 0;

        if (attempts >= MAX_VERIFY_ATTEMPTS) {
            log.warn("인증번호 입력 횟수 초과: phoneNumber={}, attempts={}", phoneNumber, attempts);
            throw new BusinessException(ErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED);
        }
    }

    // ========== Private 메서드: Redis 카운트 관리 ==========

    /**
     * 요청 횟수 증가 (전화번호, IP)
     */
    private void incrementRequestCount(String phoneNumber, String ipAddress) {
        String countKey = COUNT_KEY_PREFIX + phoneNumber;
        String ipKey = IP_COUNT_KEY_PREFIX + ipAddress;

        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, COUNT_TTL_SECONDS, TimeUnit.SECONDS);

        redisTemplate.opsForValue().increment(ipKey);
        redisTemplate.expire(ipKey, COUNT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 마지막 요청 시간 업데이트
     */
    private void updateLastRequestTime(String phoneNumber) {
        String lastKey = LAST_REQUEST_KEY_PREFIX + phoneNumber;
        String currentTime = String.valueOf(System.currentTimeMillis());
        redisTemplate.opsForValue().set(lastKey, currentTime, Duration.ofSeconds(RATE_LIMIT_SECONDS));
    }

    /**
     * 인증 시도 횟수 증가
     */
    private void incrementVerifyAttempts(String phoneNumber) {
        String attemptKey = ATTEMPT_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, CODE_TTL_SECONDS, TimeUnit.SECONDS); // 인증번호와 동일한 TTL
    }

    /**
     * 인증 시도 횟수 초기화
     */
    private void resetVerifyAttempts(String phoneNumber) {
        String attemptKey = ATTEMPT_KEY_PREFIX + phoneNumber;
        redisTemplate.delete(attemptKey);
    }
}
