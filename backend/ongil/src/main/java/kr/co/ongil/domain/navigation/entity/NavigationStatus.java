package kr.co.ongil.domain.navigation.entity;

public enum NavigationStatus {
    ACTIVE("진행 중"),
    COMPLETED("정상 도착"),
    CANCELLED("중도 취소"),
    TIMEOUT("시간 초과"),
    DEVIATION("경로 이탈");

    private final String description;

    NavigationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}