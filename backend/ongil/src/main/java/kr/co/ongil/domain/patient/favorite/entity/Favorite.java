package kr.co.ongil.domain.patient.favorite.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "favorite", uniqueConstraints = {
    @UniqueConstraint(name = "uk_patient_place", columnNames = {"patient_id", "latitude", "longitude", "place_name"})
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(name = "place_alias", length = 100)
    private String placeAlias;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "count", nullable = false)
    @Builder.Default
    private Integer count = 0;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    public void incrementCount() {
        this.count++;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public void update(String placeName, String placeAlias, String category, String address,
        Double latitude, Double longitude, Boolean isDefault) {
        if (placeName != null) this.placeName = placeName;
        if (placeAlias != null) this.placeAlias = placeAlias;
        if (category != null) this.category = category;
        if (address != null) this.address = address;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (isDefault != null) this.isDefault = isDefault;
    }
}