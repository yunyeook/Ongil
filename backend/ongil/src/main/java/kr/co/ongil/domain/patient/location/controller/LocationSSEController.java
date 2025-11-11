package kr.co.ongil.domain.patient.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Security;
import kr.co.ongil.domain.patient.location.service.LocationSSEService;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * GPS 위치 SSE 스트리밍 컨트롤러
 */
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Location SSE API", description = "보호자용 실시간 GPS 위치 스트리밍 API")
public class LocationSSEController {

    private final LocationSSEService locationSSEService;

    /**
     * 보호자용 GPS 스트림 연결 등록
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "GPS 실시간 스트림 연결 등록",
        description = """
        보호자가 연결된 환자들의 GPS 위치를 실시간으로 수신하기 위한 SSE 연결을 등록합니다.

        - SSE(Server-Sent Events) 방식 사용
        - 1회 호출 후 서버와 연결이 유지됩니다 (지속 스트리밍)
        - 클라이언트는 연결을 유지하며 서버로부터 이벤트를 실시간으로 받습니다
        - 재연결 시 동일한 API를 다시 호출해야 합니다

        **이벤트 타입**
        - `connected`: 연결 성공 확인 메시지
        - `gps-update`: 환자의 GPS 위치 업데이트 (patientId, coordinate 포함)
        """
    )
    public SseEmitter streamGPS(
    ) {
        Integer guardianId = SecurityUtil.getCurrentUserId();
        return locationSSEService.createEmitter(guardianId);
    }
}