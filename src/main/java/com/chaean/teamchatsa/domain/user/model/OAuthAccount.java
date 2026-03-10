package com.chaean.teamchatsa.domain.user.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "oauth_account")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE oauth_account SET deleted_at = NOW() WHERE id = ?")
public class OAuthAccount extends BaseEntity {
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
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private OAuthProvider provider;

    /** 도메인 메서드: 프로필 동기화 */
    public void syncProfile(String emailFromProvider, String nickname, String imageUrl) {
        if (emailFromProvider != null) this.emailFromProvider = emailFromProvider;
        if (nickname != null) this.profileNickname = nickname;
        if (imageUrl != null) this.profileImageUrl = imageUrl;
    }


    public static OAuthAccount kakaoLink(Long userId, String providerUserId, String emailFromProvider, 
                                         String profileNickname, String profileImageUrl
    ) {
        OAuthAccount account = new OAuthAccount();
        account.userId = userId;
        account.provider = OAuthProvider.KAKAO;
        account.providerUserId = providerUserId;
        account.emailFromProvider = emailFromProvider;
        account.profileNickname = profileNickname;
        account.profileImageUrl = profileImageUrl;
        account.connectedAt = LocalDateTime.now();
        return account;
    }
}