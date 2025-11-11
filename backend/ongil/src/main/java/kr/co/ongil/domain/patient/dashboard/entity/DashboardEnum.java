package kr.co.ongil.domain.patient.dashboard.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DashboardEnum {
    INCREASE("증가"), DECREASE("감소"), SAME("-");
    private final String descripton;
}
