package kr.co.ongil.domain.navigation.entity;

public enum InitiatedBy {
    PATIENT("환자"),
    GUARDIAN("보호자"),
    EMERGENT("응급시 보호자")
    ;

    private final String description;

    InitiatedBy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}