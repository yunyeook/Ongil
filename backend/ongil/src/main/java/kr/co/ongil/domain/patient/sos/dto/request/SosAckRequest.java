package kr.co.ongil.domain.patient.sos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "SosAckRequest", description = "SOS 음성재생 완료 콜백 요청")

public record SosAckRequest(
    @NotBlank(message = "sos식별자는 필수입니다.")
    @Schema(description = "sos 고유 식별자", example = "1")
    Integer sosId,

    @NotBlank(message = "재생여부는 필수입니다.")
    @Schema(description = "재생완료여부", example = "true")
    boolean played
){

}
