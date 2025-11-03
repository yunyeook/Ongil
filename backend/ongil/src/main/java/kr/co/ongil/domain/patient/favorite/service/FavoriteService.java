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
    // private final RelationshipRepository relationshipRepository; // 필요시 주입

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

        // 5. 저장
        Favorite favorite = Favorite.builder()
            .patient(patient)
            .placeName(request.placeName())
            .placeAlias(request.getPlaceAlias())
            .category(request.category())
            .address(request.address())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .isDefault(shouldBeDefault)
            .build();

        Favorite saved = favoriteRepository.save(favorite);
        log.info("즐겨찾기 생성 완료 - patientId: {}, favoriteId: {}", patientId, saved.getId());

        return FavoriteResponse.from(saved);
    }

    public FavoriteListResponse getFavorites(Integer patientId, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 조회 (count 내림차순)
        List<FavoriteResponse> favorites = favoriteRepository.findAllByPatientIdOrderByCountDesc(patientId)
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

        favoriteRepository.delete(favorite);
        log.info("즐겨찾기 삭제 완료 - favoriteId: {}", favoriteId);
    }

    @Transactional
    public FavoriteResponse setDefaultFavorite(Integer patientId, Integer favoriteId, Integer callerId) {
        // 권한 검증
        validateAccess(patientId, callerId);

        // 기존 기본 목적지 해제
        favoriteRepository.findByPatientIdAndIsDefaultTrue(patientId)
            .ifPresent(Favorite::unsetDefault);

        // 새로운 기본 목적지 설정
        Favorite favorite = favoriteRepository.findByIdAndPatientId(favoriteId, patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

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

    private void validateAccess(Integer patientId, Integer callerId) {
        // TODO: 본인 또는 보호자 관계 확인
        // 임시로 본인만 허용
        if (!patientId.equals(callerId)) {
            throw new BusinessException(ErrorCode.FAVORITE_ACCESS_DENIED);
        }

        // 보호자 권한 확인이 필요한 경우:
        // boolean isCaregiver = relationshipRepository.existsByPatientIdAndCaregiverId(patientId, callerId);
        // if (!patientId.equals(callerId) && !isCaregiver) {
        //     throw new BusinessException(ErrorCode.FAVORITE_ACCESS_DENIED);
        // }
    }
}