package kr.co.ongil.domain.patient.sos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.patient.sos.dto.request.SosAckRequest;
import kr.co.ongil.domain.patient.sos.dto.response.SosResponse;
import kr.co.ongil.domain.patient.sos.service.SosService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "SOS API", description = "환자 도움 요청(SOS) API")
public class SosController {

    private final SosService sosService;

    /**
     * SOS 요청 생성
     */
    @PostMapping("/{patientId}/sos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation( summary = "SOS 음성 재생 요청 전송", description = "보호자가 환자에게 sos 음성을 재생하도록 요청합니다.")
    public ApiResponse<SosResponse> createSosRequest(
        @Parameter(description = "도움 요청 음성을 재생할 대상 환자 고유 ID", example = "1", required = true)
        @PathVariable Integer patientId
    ) {
        Integer guardianId = SecurityUtil.getCurrentUserId();
//        Integer guardianId = 2; //TODO : 개발시
        SosResponse response = sosService.createSosRequest(guardianId, patientId);
        return ApiResponse.success(ResponseMessage.SOS_REQUEST_CREATED, response);
    }

    /**
     * SOS 음성 재생 완료 콜백
     */
    @PostMapping("/sos/{sosId}/ack")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "SOS 음성 재생 완료 콜백",
        description = "환자의 워치가 SOS 음성 재생 완료를 응답합니다."
    )
    public ApiResponse<String> createSosCallbackAckRequest(
        @Parameter(description = "SOS 음성재생 완료한 sos 고유 ID", example = "1", required = true)
        @PathVariable Integer sosId
    ) {
    Integer patientId = SecurityUtil.getCurrentUserId();
//        Integer patientId = 5; //TODO : 개발시

        sosService.createSosCallbackAckRequest(patientId, sosId);
        return ApiResponse.success(ResponseMessage.SOS_ACK_PROCESSED);
    }


}