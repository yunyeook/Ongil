package kr.co.ongil.domain.call.entity;

import jakarta.persistence.*;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통화 녹음 메타데이터 엔티티
 * 실제 녹음 파일은 클라이언트 로컬에 저장되며, 서버에는 메타데이터만 저장됩니다.
 */
@Entity
@Table(name = "call_recording")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallRecording extends BaseEntity {

    /**
     * 연관된 통화 기록
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_log_id", nullable = false)
    private CallLog callLog;

    /**
     * 로컬 파일 경로 (클라이언트 디바이스 내 경로)
     */
    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    /**
     * 파일 크기 (바이트)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 녹음 길이 (초)
     */
    @Column
    private Integer duration;

    // 비즈니스 메서드

    /**
     * 파일 정보 업데이트
     */
    public void updateFileInfo(Long fileSize, Integer duration) {
        this.fileSize = fileSize;
        this.duration = duration;
    }
}
