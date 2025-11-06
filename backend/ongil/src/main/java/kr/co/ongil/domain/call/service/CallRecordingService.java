package kr.co.ongil.domain.call.service;

import kr.co.ongil.domain.call.dto.request.CreateCallRecordingRequest;
import kr.co.ongil.domain.call.dto.response.CallRecordingResponse;
import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallRecording;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.call.repository.CallRecordingRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통화 녹음 메타데이터 서비스
 * 실제 녹음 파일은 클라이언트 로컬에 저장되며,
 * 서버에는 메타데이터만 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallRecordingService {

    private final CallRecordingRepository callRecordingRepository;
    private final CallLogRepository callLogRepository;

    /**
     * 통화 녹음 메타데이터 생성
     */
    @Transactional
    public CallRecordingResponse createCallRecording(CreateCallRecordingRequest request) {
        log.info("통화 녹음 메타데이터 생성: callLogId={}", request.callLogId());

        // 1. 통화 로그 조회
        CallLog callLog = callLogRepository.findById(request.callLogId())
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_LOG_NOT_FOUND));

        // 2. 이미 녹음이 존재하는지 확인
        if (callRecordingRepository.existsByCallLog(callLog)) {
            throw new BusinessException(ErrorCode.CALL_RECORDING_ALREADY_EXISTS);
        }

        // 3. 녹음 메타데이터 생성
        CallRecording recording = CallRecording.builder()
            .callLog(callLog)
            .filePath(request.filePath())
            .fileSize(request.fileSize())
            .duration(request.duration())
            .build();

        CallRecording savedRecording = callRecordingRepository.save(recording);
        log.info("통화 녹음 메타데이터 생성 완료: recordingId={}", savedRecording.getId());

        return CallRecordingResponse.from(savedRecording);
    }

    /**
     * 통화 녹음 정보 조회 (녹음 ID로)
     */
    @Transactional(readOnly = true)
    public CallRecordingResponse getCallRecording(Integer recordingId) {
        log.info("통화 녹음 정보 조회: recordingId={}", recordingId);

        CallRecording recording = callRecordingRepository.findById(recordingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_RECORDING_NOT_FOUND));

        return CallRecordingResponse.from(recording);
    }

    /**
     * 통화 로그로 녹음 정보 조회
     */
    @Transactional(readOnly = true)
    public CallRecordingResponse getCallRecordingByCallLogId(Integer callLogId) {
        log.info("통화 로그로 녹음 정보 조회: callLogId={}", callLogId);

        CallRecording recording = callRecordingRepository.findByCallLog_Id(callLogId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_RECORDING_NOT_FOUND));

        return CallRecordingResponse.from(recording);
    }

    /**
     * 통화 녹음 메타데이터 업데이트
     */
    @Transactional
    public CallRecordingResponse updateCallRecording(Integer recordingId, Long fileSize, Integer duration) {
        log.info("통화 녹음 메타데이터 업데이트: recordingId={}", recordingId);

        CallRecording recording = callRecordingRepository.findById(recordingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_RECORDING_NOT_FOUND));

        recording.updateFileInfo(fileSize, duration);
        CallRecording updatedRecording = callRecordingRepository.save(recording);

        log.info("통화 녹음 메타데이터 업데이트 완료: recordingId={}", recordingId);

        return CallRecordingResponse.from(updatedRecording);
    }

    /**
     * 통화 녹음 메타데이터 삭제
     */
    @Transactional
    public void deleteCallRecording(Integer recordingId) {
        log.info("통화 녹음 메타데이터 삭제: recordingId={}", recordingId);

        CallRecording recording = callRecordingRepository.findById(recordingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_RECORDING_NOT_FOUND));

        callRecordingRepository.delete(recording);
        log.info("통화 녹음 메타데이터 삭제 완료: recordingId={}", recordingId);
    }
}
