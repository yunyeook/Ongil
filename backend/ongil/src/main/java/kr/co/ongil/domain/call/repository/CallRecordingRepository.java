package kr.co.ongil.domain.call.repository;

import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallRecording;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 통화 녹음 메타데이터 레포지토리
 */
public interface CallRecordingRepository extends JpaRepository<CallRecording, Integer> {

    /**
     * 통화 로그로 녹음 정보 조회
     */
    Optional<CallRecording> findByCallLog(CallLog callLog);

    /**
     * 통화 로그로 녹음 존재 여부 확인
     */
    boolean existsByCallLog(CallLog callLog);

    /**
     * 통화 로그 ID로 녹음 정보 조회
     */
    Optional<CallRecording> findByCallLog_Id(Integer callLogId);
}
