package com.chaean.teamchatsa.domain.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "oauth_account")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false) // FK
    private Long userId;

    @Size(max = 100)
    @NotNull
    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Size(max = 100)
    @Column(name = "email_from_provider", length = 100)
    private String emailFromProvider;

    @Size(max = 100)
    @Column(name = "profile_nickname", length = 100)
    private String profileNickname;

    @Size(max = 255)
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private OAuthProvider provider;

    /** 도메인 메서드: 프로필 동기화 */
    public void syncProfile(String emailFromProvider, String nickname, String imageUrl, LocalDateTime now) {
        if (emailFromProvider != null) this.emailFromProvider = emailFromProvider;
        if (nickname != null) this.profileNickname = nickname;
        if (imageUrl != null) this.profileImageUrl = imageUrl;
        this.updatedAt = now;
    }


    public static OAuthAccount kakaoLink(Long userId, String providerUserId, String emailFromProvider, 
                                         String profileNickname, String profileImageUrl, LocalDateTime now
    ) {
        return OAuthAccount.builder()
                .userId(userId)
                .provider(OAuthProvider.KAKAO)
                .providerUserId(providerUserId)
                .emailFromProvider(emailFromProvider)
                .profileNickname(profileNickname)
                .profileImageUrl(profileImageUrl)
                .connectedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}