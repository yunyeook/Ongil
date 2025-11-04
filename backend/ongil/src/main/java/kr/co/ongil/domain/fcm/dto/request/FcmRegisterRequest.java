package kr.co.ongil.domain.fcm.dto.request;

public record FcmRegisterRequest (
     Integer userId,
     String token,
     String deviceInfo
){}