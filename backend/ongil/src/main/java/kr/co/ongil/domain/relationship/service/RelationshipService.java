package kr.co.ongil.domain.relationship.service;

import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.entity.NotificationType;
import kr.co.ongil.domain.notification.service.NotificationService;
import kr.co.ongil.domain.relationship.dto.request.CreateRelationshipRequest;
import kr.co.ongil.domain.relationship.dto.request.UpdateRelationshipRequest;
import kr.co.ongil.domain.relationship.dto.response.RelationshipResponse;
import kr.co.ongil.domain.relationship.entity.Relationship;
import kr.co.ongil.domain.relationship.entity.RelationshipType;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.entity.UserType;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.domain.verification.entity.VerificationGrant;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    private static final String USED_TOKEN_KEY_PREFIX = "used:token:";

    /**
     * 관계 등록 (verificationToken 사용)
     */
    @Transactional
    public RelationshipResponse createRelationship(Integer userId, CreateRelationshipRequest request) {
        log.info("==================== 관계 등록 요청 시작 ====================");
        log.info("userId: {}", userId);
        log.info("verificationToken 앞 50자: {}",
                request.verificationToken() != null ?
                request.verificationToken().substring(0, Math.min(50, request.verificationToken().length())) : "null");

        // 1. 요청자 조회
        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        log.info("요청자 조회 완료 - userName: {}, userType: {}", requestUser.getName(), requestUser.getUserType());

        // 2. 토큰 검증 및 상대방 전화번호 추출
        String verificationToken = request.verificationToken();
        log.info("토큰 검증 시작...");
        validateVerificationToken(verificationToken);
        log.info("토큰 검증 성공!");

        String counterpartPhoneNumber = jwtUtil.getPhoneNumberFromToken(verificationToken);
        String grant = jwtUtil.getGrantFromToken(verificationToken);

        // 3. Grant 검증 (RELATIONSHIP만 허용)
        if (!VerificationGrant.RELATIONSHIP.name().equals(grant)) {
            log.warn("잘못된 Grant 토큰 사용 - grant: {}", grant);
            throw new BusinessException(ErrorCode.INVALID_RELATIONSHIP_GRANT);
        }

        // 4. 상대방 사용자 조회
        User counterpartUser = userRepository.findByPhoneNumber(counterpartPhoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTERPART_USER_NOT_FOUND));

        // 5. 자기 자신과의 관계 등록 방지
        if (requestUser.getId().equals(counterpartUser.getId())) {
            log.warn("자기 자신과의 관계 등록 시도 - userId: {}", userId);
            throw new BusinessException(ErrorCode.SELF_RELATIONSHIP_NOT_ALLOWED);
        }

        // 6. 동일한 UserType 간 관계 등록 방지
        if (requestUser.getUserType() == counterpartUser.getUserType()) {
            log.warn("동일한 UserType 간 관계 등록 시도 - requestUserType: {}, counterpartUserType: {}",
                    requestUser.getUserType(), counterpartUser.getUserType());
            throw new BusinessException(ErrorCode.SAME_USER_TYPE_RELATIONSHIP);
        }

        // 7. 중복 관계 체크
        if (relationshipRepository.existsByGuardianAndPatient(requestUser, counterpartUser)) {
            log.warn("이미 존재하는 관계 - user1: {}, user2: {}", userId, counterpartUser.getId());
            throw new BusinessException(ErrorCode.RELATIONSHIP_ALREADY_EXISTS);
        }

        // 8. 토큰 소비 (Redis에 사용 기록 저장)
        markTokenAsUsed(verificationToken);

        // 9. Relationship 생성 (보호자/환자 역할 자동 매핑)
        User guardian = requestUser.getUserType() == UserType.GUARDIAN ? requestUser : counterpartUser;
        User patient = requestUser.getUserType() == UserType.PATIENT ? requestUser : counterpartUser;

        // 10. String → RelationshipType enum 변환 및 검증
        RelationshipType requestUserType = RelationshipType.fromDescription(request.relationshipType());
        RelationshipType counterpartType = requestUserType.getCounterpart();

        log.info("관계 유형 변환 완료 - 요청자: {}, 상대방: {}",
                requestUserType.getDescription(), counterpartType.getDescription());

        Relationship relationship = Relationship.builder()
                .guardian(guardian)
                .patient(patient)
                .build();

        // 11. 양방향 관계 정보 설정
        // 요청자 입장에서의 관계 설정
        relationship.updateRelationshipInfo(requestUser, request.relationshipName(), requestUserType);
        // 상대방 입장에서의 관계 자동 설정 (반대 관계 유형)
        relationship.updateRelationshipInfo(counterpartUser, null, counterpartType);

        // 12. 정렬 순서 자동 계산 (각자의 마지막 순서 + 1)
        Integer guardianMaxOrder = relationshipRepository.findMaxOrderByGuardian(guardian);
        Integer patientMaxOrder = relationshipRepository.findMaxOrderByPatient(patient);
        relationship.setDisplayOrder(guardian, guardianMaxOrder + 1);
        relationship.setDisplayOrder(patient, patientMaxOrder + 1);

        log.info("정렬 순서 설정 완료 - 보호자 순서: {}, 환자 순서: {}",
                guardianMaxOrder + 1, patientMaxOrder + 1);

        relationshipRepository.save(relationship);
        log.info("관계 등록 완료 - relationshipId: {}, guardian: {}, patient: {}, 요청자 타입: {}, 상대방 타입: {}",
                relationship.getId(), guardian.getId(), patient.getId(),
                requestUserType.getDescription(), counterpartType.getDescription());

        // 11. 상대방에게 알림 전송
        sendRelationshipNotification(requestUser, counterpartUser,relationship.getId());

        return RelationshipResponse.from(relationship, requestUser);
    }

    /**
     * 내 관계 목록 조회 (정렬 순서 적용)
     */
    @Transactional(readOnly = true)
    public List<RelationshipResponse> getMyRelationships(Integer userId) {
        log.info("관계 목록 조회 요청 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // UserType에 따라 다른 Repository 쿼리 사용 (정렬 포함)
        List<Relationship> relationships;
        if (user.getUserType() == UserType.GUARDIAN) {
            relationships = relationshipRepository.findByGuardianOrderByOrder(user);
        } else {
            relationships = relationshipRepository.findByPatientOrderByOrder(user);
        }

        return relationships.stream()
                .map(relationship -> RelationshipResponse.from(relationship, user))
                .toList();
    }

    /**
     * 관계 상세 조회
     */
    @Transactional(readOnly = true)
    public RelationshipResponse getRelationshipDetail(Integer userId, Integer relationshipId) {
        log.info("관계 상세 조회 요청 - userId: {}, relationshipId: {}", userId, relationshipId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Relationship relationship = relationshipRepository.findByIdAndUser(relationshipId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELATIONSHIP_NOT_FOUND));

        return RelationshipResponse.from(relationship, user);
    }

    /**
     * 관계 정보 수정
     */
    @Transactional
    public RelationshipResponse updateRelationship(Integer userId, Integer relationshipId,
                                                   UpdateRelationshipRequest request) {
        log.info("관계 정보 수정 요청 - userId: {}, relationshipId: {}", userId, relationshipId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Relationship relationship = relationshipRepository.findByIdAndUser(relationshipId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELATIONSHIP_NOT_FOUND));

        // String → RelationshipType enum 변환 (relationshipType이 null이 아닌 경우에만)
        RelationshipType relationshipType = null;
        if (request.relationshipType() != null) {
            relationshipType = RelationshipType.fromDescription(request.relationshipType());
            log.info("관계 유형 변환 완료 - {}", relationshipType.getDescription());
        }

        // 비즈니스 로직: 요청자에 따라 다른 필드 업데이트
        relationship.updateRelationshipInfo(user, request.relationshipName(), relationshipType);

        // 정렬 순서 업데이트 (요청된 경우에만)
        if (request.displayOrder() != null) {
            relationship.setDisplayOrder(user, request.displayOrder());
            log.info("정렬 순서 수정 완료 - displayOrder: {}", request.displayOrder());
        }

        log.info("관계 정보 수정 완료 - relationshipId: {}", relationshipId);

        return RelationshipResponse.from(relationship, user);
    }

    /**
     * 관계 삭제 (양방향 해제 + 정렬 순서 재정렬)
     */
    @Transactional
    public void deleteRelationship(Integer userId, Integer relationshipId) {
        log.info("관계 삭제 요청 - userId: {}, relationshipId: {}", userId, relationshipId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Relationship relationship = relationshipRepository.findByIdAndUser(relationshipId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELATIONSHIP_NOT_FOUND));

        // 삭제 전에 정렬 순서 저장
        Integer deletedOrder = relationship.getDisplayOrder(user);

        relationshipRepository.delete(relationship);
        log.info("관계 삭제 완료 - relationshipId: {}", relationshipId);

        // 삭제 후 재정렬 (삭제된 순서 이후의 관계들을 -1씩)
        if (deletedOrder != null) {
            resequenceAfterDelete(user, deletedOrder);
        }
    }

    /**
     * 관계 목록 일괄 재정렬
     */
    @Transactional
    public List<RelationshipResponse> reorderMyRelationships(Integer userId, List<Integer> orderedRelationshipIds) {
        log.info("관계 목록 일괄 재정렬 요청 - userId: {}, count: {}", userId, orderedRelationshipIds.size());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Pessimistic Lock으로 관계 목록 조회 (동시성 제어)
        List<Relationship> relationships;
        if (user.getUserType() == UserType.GUARDIAN) {
            relationships = relationshipRepository.findByGuardianWithLock(user);
        } else {
            relationships = relationshipRepository.findByPatientWithLock(user);
        }

        // ID로 매핑 (빠른 조회를 위해)
        var relationshipMap = relationships.stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.getId(),
                        r -> r
                ));

        // 정렬 순서 업데이트 (orderedRelationshipIds 순서대로 1, 2, 3, ...)
        for (int i = 0; i < orderedRelationshipIds.size(); i++) {
            Integer relationshipId = orderedRelationshipIds.get(i);
            Relationship relationship = relationshipMap.get(relationshipId);

            if (relationship == null) {
                log.warn("존재하지 않거나 권한이 없는 관계 ID: {}", relationshipId);
                throw new BusinessException(ErrorCode.RELATIONSHIP_NOT_FOUND);
            }

            relationship.setDisplayOrder(user, i + 1);  // 1부터 시작
            log.debug("정렬 순서 업데이트 - relationshipId: {}, order: {}", relationshipId, i + 1);
        }

        log.info("관계 목록 일괄 재정렬 완료");

        // 정렬된 결과 반환
        return orderedRelationshipIds.stream()
                .map(relationshipMap::get)
                .map(relationship -> RelationshipResponse.from(relationship, user))
                .toList();
    }

    /**
     * 대표 관계 설정
     */
    @Transactional
    public RelationshipResponse setDefaultRelationship(Integer userId, Integer relationshipId) {
        log.info("대표 관계 설정 요청 - userId: {}, relationshipId: {}", userId, relationshipId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Relationship relationship = relationshipRepository.findByIdAndUser(relationshipId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELATIONSHIP_NOT_FOUND));

        // 1. 기존 대표 관계 모두 해제
        if (user.getUserType() == UserType.GUARDIAN) {
            relationshipRepository.clearDefaultForGuardian(user);
        } else {
            relationshipRepository.clearDefaultForPatient(user);
        }

        // 2. 새로운 대표 관계 설정
        relationship.setDefault(user, true);
        log.info("대표 관계 설정 완료 - relationshipId: {}", relationshipId);

        return RelationshipResponse.from(relationship, user);
    }

    // ========== Private 메서드 ==========

    /**
     * 토큰 검증
     */
    private void validateVerificationToken(String token) {
        log.info("토큰 검증 시작 - token length: {}, token prefix: {}",
                token.length(), token.substring(0, Math.min(50, token.length())));

        // 1. 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {
            log.warn("유효하지 않은 토큰 - 서명 검증 실패 또는 파싱 오류");
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        // 2. 토큰 만료 여부 확인
        if (jwtUtil.isTokenExpired(token)) {
            log.warn("만료된 토큰");
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        // 3. 이미 사용된 토큰인지 확인 (Redis)
        String tokenKey = USED_TOKEN_KEY_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
            log.warn("이미 사용된 토큰");
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        log.info("토큰 검증 완료");
    }

    /**
     * 토큰 소비 처리 (Redis에 사용 기록)
     */
    private void markTokenAsUsed(String token) {
        String tokenKey = USED_TOKEN_KEY_PREFIX + token;
        long remainingExpiration = jwtUtil.getRemainingExpiration(token);

        // Redis에 사용된 토큰 저장 (TTL: 토큰의 남은 만료 시간)
        redisTemplate.opsForValue().set(tokenKey, "used", remainingExpiration, TimeUnit.MILLISECONDS);
        log.info("토큰 소비 처리 완료 - ttl: {}ms", remainingExpiration);
    }

    /**
     * 관계 등록 알림 전송
     */
    private void sendRelationshipNotification(User sender, User receiver,Integer relationshipId) {
        try {
            NotificationRequest notificationRequest = NotificationRequest.of(
                    NotificationType.RELATIONSHIP_REGIST.getDescription(),
                    sender.getName() + "님과 관계가 등록되었습니다.",
                    NotificationType.RELATIONSHIP_REGIST,
                    sender.getId(),
                    receiver.getId()
            );
            notificationService.createNotifications(notificationRequest,relationshipId);
            log.info("관계 등록 알림 전송 완료 - receiver: {}", receiver.getId());
        } catch (Exception e) {
            log.error("관계 등록 알림 전송 실패", e);
            // 알림 실패는 관계 등록을 막지 않음
        }
    }

    /**
     * 삭제 후 정렬 순서 재정렬 (삭제된 순서 이후의 관계들을 -1씩 이동)
     */
    private void resequenceAfterDelete(User user, Integer deletedOrder) {
        log.info("삭제 후 재정렬 시작 - deletedOrder: {}", deletedOrder);

        // 사용자의 모든 관계 조회
        List<Relationship> relationships;
        if (user.getUserType() == UserType.GUARDIAN) {
            relationships = relationshipRepository.findByGuardianOrderByOrder(user);
        } else {
            relationships = relationshipRepository.findByPatientOrderByOrder(user);
        }

        // 삭제된 순서보다 큰 순서를 가진 관계들을 -1씩 이동
        relationships.stream()
                .filter(r -> {
                    Integer order = r.getDisplayOrder(user);
                    return order != null && order > deletedOrder;
                })
                .forEach(r -> {
                    Integer currentOrder = r.getDisplayOrder(user);
                    r.setDisplayOrder(user, currentOrder - 1);
                    log.debug("정렬 순서 재정렬 - relationshipId: {}, {} -> {}",
                            r.getId(), currentOrder, currentOrder - 1);
                });

        log.info("삭제 후 재정렬 완료");
    }
}
