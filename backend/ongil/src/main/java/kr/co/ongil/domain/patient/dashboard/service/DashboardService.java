package kr.co.ongil.domain.patient.dashboard.service;

import jakarta.transaction.Transactional;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.patient.abnormal.repository.AbnormalRepository;
import kr.co.ongil.domain.patient.dashboard.dto.DashboardResponseDto;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.dashboard.repository.DashboardRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final AbnormalRepository abnormalRepository;
    private final CallLogRepository callLogRepository;
    
    public DashboardResponseDto getDashboardResponseDto(Integer patientId) {
        Optional<DashboardCalc> dashboardCalc = dashboardRepository.findById(patientId);
        if(dashboardCalc.isPresent()) return DashboardResponseDto.from(dashboardCalc.get());
        else return null;
    }

    public void aggregateWeekly() {
        DashboardCalc calc=DashboardCalc.builder().build();
    }
}
