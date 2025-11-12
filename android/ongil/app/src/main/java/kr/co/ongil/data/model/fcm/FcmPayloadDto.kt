package kr.co.ongil.data.model.fcm

import com.google.gson.annotations.SerializedName

/**
 * FCM의 'data' 페이로드로부터 수신된 데이터를 매핑하기 위한 Data Transfer Object (DTO).
 * 이 클래스의 프로퍼티는 서버에서 전송하는 JSON 데이터의 키(key)에 해당합니다.
 * 모든 프로퍼티는 데이터 누락 가능성에 대비하여 Nullable 타입으로 선언합니다.
 */
data class FcmPayloadDto(
    /**
     * 푸시 메시지의 종류를 나타내는 식별자.
     */
    @SerializedName("type")
    val type: String?,

    /**
     * 알림에 표시될 제목.
     */
    @SerializedName("title")
    val title: String?,

    /**
     * 메시지를 전송한 주체의 ID.
     * (보호자 또는 환자가 될 수 있음)
     */
    @SerializedName("senderId")
    val senderId: String?,

    /**
     * 메시지를 수신해야 하는 대상의 ID.
     */
    @SerializedName("receiverId")
    val receiverId: String?,

    /**
     * 알림에 표시될 내용.
     */
    @SerializedName("content")
    val content: String?,

    /**
     * DB 알림 테이블 고유 ID.
     */
    @SerializedName("notificationId")
    val notificationId: String?,

    /**
     * 해당 알림과 관련된 테이블의 고유 ID.
     */
    @SerializedName("relatedTableId")
    val relatedTableId: String?
)