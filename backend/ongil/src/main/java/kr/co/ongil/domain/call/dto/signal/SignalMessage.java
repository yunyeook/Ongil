package kr.co.ongil.domain.call.dto.signal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * WebRTC 시그널링 메시지
 * VoIP 통화를 위한 OFFER/ANSWER/ICE 교환용
 */
@Schema(description = "WebRTC 시그널링 메시지")
public record SignalMessage(

    @Schema(
        description = "시그널 타입",
        example = "OFFER",
        allowableValues = {"INCOMING", "ACCEPT", "REJECT", "OFFER", "ANSWER", "ICE", "HANGUP"}
    )
    String type,

    @Schema(description = "SDP (Session Description Protocol) - OFFER/ANSWER 시 사용")
    String sdp,

    @Schema(description = "ICE candidate - ICE 교환 시 사용")
    String candidate,

    @Schema(description = "SDP media stream ID")
    String sdpMid,

    @Schema(description = "SDP media stream line index")
    Integer sdpMLineIndex,

    @Schema(description = "통화 ID", example = "1")
    Integer callId,

    @Schema(description = "발신자 사용자 ID", example = "1")
    Integer fromUserId,

    @Schema(description = "수신자 사용자 ID", example = "2")
    Integer toUserId
) {

    /**
     * INCOMING 메시지 생성 (통화 요청)
     */
    public static SignalMessage incoming(Integer callId, Integer fromUserId, Integer toUserId) {
        return new SignalMessage(
            "INCOMING",
            null, null, null, null,
            callId, fromUserId, toUserId
        );
    }

    /**
     * ACCEPT 메시지 생성 (통화 수락)
     */
    public static SignalMessage accept(Integer callId, Integer fromUserId, Integer toUserId) {
        return new SignalMessage(
            "ACCEPT",
            null, null, null, null,
            callId, fromUserId, toUserId
        );
    }

    /**
     * REJECT 메시지 생성 (통화 거절)
     */
    public static SignalMessage reject(Integer callId, Integer fromUserId, Integer toUserId) {
        return new SignalMessage(
            "REJECT",
            null, null, null, null,
            callId, fromUserId, toUserId
        );
    }

    /**
     * OFFER 메시지 생성
     */
    public static SignalMessage offer(String sdp, Integer callId, Integer fromUserId, Integer toUserId) {
        return new SignalMessage(
            "OFFER",
            sdp, null, null, null,
            callId, fromUserId, toUserId
        );
    }

    /**
     * ANSWER 메시지 생성
     */
    public static SignalMessage answer(String sdp, Integer callId, Integer fromUserId, Integer toUserId) {
        return new SignalMessage(
            "ANSWER",
            sdp, null, null, null,
            callId, fromUserId, toUserId
        );
    }

    /**
     * ICE 메시지 생성
     */
    public static SignalMessage ice(
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex,
        Integer callId,
        Integer fromUserId,
        Integer toUserId
    ) {
        return new SignalMessage(
            "ICE",
            null, candidate, sdpMid, sdpMLineIndex,
            callId, fromUserId, toUserId
        );
    }

    /**
     * HANGUP 메시지 생성 (통화 종료)
     */
    public static SignalMessage hangup(Integer callId, Integer fromUserId, Integer toUserId) {
        return new SignalMessage(
            "HANGUP",
            null, null, null, null,
            callId, fromUserId, toUserId
        );
    }
}
