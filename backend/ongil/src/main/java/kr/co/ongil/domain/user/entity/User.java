package kr.co.ongil.domain.user.entity;

import jakarta.persistence.*;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import kr.co.ongil.domain.user.entity.Provider;
import kr.co.ongil.domain.user.entity.UserType;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_member_id")
    private String providerMemberId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 15)
    private String birth;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 30)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 비즈니스 메서드
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}