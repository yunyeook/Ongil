package kr.co.ongil.domain.patient.favorite.service;

import kr.co.ongil.domain.patient.favorite.dto.request.CreateFavoriteRequest;
import kr.co.ongil.domain.patient.favorite.dto.request.UpdateFavoriteRequest;
import kr.co.ongil.domain.patient.favorite.dto.response.FavoriteListResponse;
import kr.co.ongil.domain.patient.favorite.dto.response.FavoriteResponse;
import kr.co.ongil.domain.patient.favorite.entity.Favorite;
import kr.co.ongil.domain.patient.favorite.repository.FavoriteRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.util.PatientAccessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PatientAccessValidator patientAccessValidator;

    @Transactional
    public FavoriteResponse createFavorite(Integer patientId, CreateFavoriteRequest request, Integer callerId) {
        // 1. 환자 존재 확인
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        // 2. 권한 검증 (본인 또는 보호자)
        validateAccess(patientId, callerId);

        // 3. 중복 체크
        if (favoriteRepository.existsByPatientIdAndLatitudeAndLongitudeAndPlaceName(
            patientId, request.latitude(), request.longitude(), request.placeName())) {
            throw new BusinessException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        // 4. 첫 등록 시 자동으로 기본 목적지 설정
        boolean isFirstFavorite = favoriteRepository.findAllByPatientId(patientId).isEmpty();
        boolean shouldBeDefault = isFirstFavorite || request.getIsDefault();

        // 5. 기본 목적지로 설정하려는 경우, 기존 default 모두 해제
        if (shouldBeDefault) {
            favoriteRepository.clearAllDefaultsByPatientId(patientId);
            log.info("기존 기본 목적지 해제 - patientId: {}", patientId);
        }

        // 6. 정렬 순서 자동 계산 (마지막 순서 + 1)
        Integer maxOrder = favoriteRepository.findMaxOrderByPatientId(patientId);
        Integer displayOrder = maxOrder + 1;

        // 7. 저장
        Favorite favorite = Favorite.builder()
            .patient(patient)
            .placeName(request.placeName())
            .placeAlias(request.getPlaceAlias())
            .category(request.category())
            .address(request.address())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .isDefault(shouldBeDefault)
            .displayOrder(displayOrder)
            .build();

        Favorite saved = favoriteRepository.save(favorite);
        log.info("즐겨찾기 생성 완료 - patientId: {}, favoriteId: {}", patientId, saved.getId());

        return FavoriteResponse.from(saved);
    }

    public FavoriteListResponse getFavorites(Integer patientId, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 조회 (정렬 순서대로, null은 맨 뒤)
        List<FavoriteResponse> favorites = favoriteRepository.findAllByPatientIdOrderByDisplayOrder(patientId)
            .stream()
            .map(FavoriteResponse::from)
            .collect(Collectors.toList());

        return FavoriteListResponse.of(favorites);
    }

    @Transactional
    public FavoriteResponse getFavorite(Integer patientId, Integer favoriteId, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 즐겨찾기 조회
        Favorite favorite = favoriteRepository.findByIdAndPatientId(favoriteId, patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public FavoriteResponse updateFavorite(Integer patientId, Integer favoriteId,
        UpdateFavoriteRequest request, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 즐겨찾기 조회
        Favorite favorite = favoriteRepository.findByIdAndPatientId(favoriteId, patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        // 업데이트할 내용이 없으면 예외
        if (!request.hasAnyUpdate()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 수정
        favorite.update(
            request.placeName(),
            request.placeAlias(),
            request.category(),
            request.address(),
            request.latitude(),
            request.longitude()
        );

        log.info("즐겨찾기 수정 완료 - favoriteId: {}", favoriteId);
        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public void deleteFavorite(Integer patientId, Integer favoriteId, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 즐겨찾기 조회
        Favorite favorite = favoriteRepository.findByIdAndPatientId(favoriteId, patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        // 삭제 전에 정렬 순서 저장
        Integer deletedOrder = favorite.getDisplayOrder();

        favoriteRepository.delete(favorite);
        log.info("즐겨찾기 삭제 완료 - favoriteId: {}", favoriteId);

        // 삭제 후 재정렬 (삭제된 순서 이후의 즐겨찾기들을 -1씩)
        if (deletedOrder != null) {
            resequenceAfterDelete(patientId, deletedOrder);
        }
    }

    @Transactional
    public FavoriteResponse setDefaultFavorite(Integer patientId, Integer favoriteId, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 기존 기본 목적지 모두 해제 (먼저 실행 - Bulk UPDATE)
        favoriteRepository.clearAllDefaultsByPatientId(patientId);
        log.info("기존 기본 목적지 모두 해제 - patientId: {}", patientId);

        // 즐겨찾기 조회 (Bulk UPDATE 후에 조회)
        Favorite favorite = favoriteRepository.findByIdAndPatientId(favoriteId, patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        // 새로운 기본 목적지 설정
        favorite.setAsDefault();
        log.info("기본 목적지 설정 완료 - favoriteId: {}", favoriteId);

        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public void incrementFavoriteCount(Integer favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        favorite.incrementCount();
        log.info("즐겨찾기 사용 횟수 증가 - favoriteId: {}, count: {}", favoriteId, favorite.getCount());
    }

    /**
     * 즐겨찾기 목록 일괄 재정렬
     */
    @Transactional
    public List<FavoriteResponse> reorderFavorites(Integer patientId, List<Integer> orderedFavoriteIds, Integer callerId) {
        log.info("즐겨찾기 목록 일괄 재정렬 요청 - patientId: {}, count: {}", patientId, orderedFavoriteIds.size());

        // 권한 검증
        validateAccess(patientId, callerId);

        // Pessimistic Lock으로 즐겨찾기 목록 조회 (동시성 제어)
        List<Favorite> favorites = favoriteRepository.findAllByPatientIdWithLock(patientId);

        // ID로 매핑 (빠른 조회를 위해)
        var favoriteMap = favorites.stream()
                .collect(Collectors.toMap(
                        f -> f.getId(),
                        f -> f
                ));

        // 정렬 순서 업데이트 (orderedFavoriteIds 순서대로 1, 2, 3, ...)
        for (int i = 0; i < orderedFavoriteIds.size(); i++) {
            Integer favoriteId = orderedFavoriteIds.get(i);
            Favorite favorite = favoriteMap.get(favoriteId);

            if (favorite == null) {
                log.warn("존재하지 않거나 권한이 없는 즐겨찾기 ID: {}", favoriteId);
                throw new BusinessException(ErrorCode.FAVORITE_NOT_FOUND);
            }

            favorite.setDisplayOrder(i + 1);  // 1부터 시작
            log.debug("정렬 순서 업데이트 - favoriteId: {}, order: {}", favoriteId, i + 1);
        }

        log.info("즐겨찾기 목록 일괄 재정렬 완료");

        // 정렬된 결과 반환
        return orderedFavoriteIds.stream()
                .map(favoriteMap::get)
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 삭제 후 정렬 순서 재정렬 (삭제된 순서 이후의 즐겨찾기들을 -1씩 이동)
     */
    private void resequenceAfterDelete(Integer patientId, Integer deletedOrder) {
        log.info("삭제 후 재정렬 시작 - patientId: {}, deletedOrder: {}", patientId, deletedOrder);

        // 환자의 모든 즐겨찾기 조회
        List<Favorite> favorites = favoriteRepository.findAllByPatientIdOrderByDisplayOrder(patientId);

        // 삭제된 순서보다 큰 순서를 가진 즐겨찾기들을 -1씩 이동
        favorites.stream()
                .filter(f -> {
                    Integer order = f.getDisplayOrder();
                    return order != null && order > deletedOrder;
                })
                .forEach(f -> {
                    Integer currentOrder = f.getDisplayOrder();
                    f.setDisplayOrder(currentOrder - 1);
                    log.debug("정렬 순서 재정렬 - favoriteId: {}, {} -> {}",
                            f.getId(), currentOrder, currentOrder - 1);
                });

        log.info("삭제 후 재정렬 완료");
    }

    private void validateAccess(Integer patientId, Integer callerId) {
        patientAccessValidator.validateAccess(patientId, callerId, ErrorCode.FAVORITE_ACCESS_DENIED);
    }
}