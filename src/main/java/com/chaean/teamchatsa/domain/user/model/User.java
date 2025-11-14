package com.chaean.teamchatsa.domain.user.model;

import com.chaean.teamchatsa.domain.team.model.Position;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateReq;
import com.chaean.teamchatsa.global.common.model.DeleteAndTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"user\"")
public class User extends DeleteAndTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 50)
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 50)
    @Column(name = "phone", length = 50)
    private String phone;

    @Size(max = 100)
    @NotNull
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "position", nullable = false)
    private Position position = Position.ALL;

    public static User of(String username, String email, String passwordHash) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordHash)
                .build();
    }

    public void update(UserUpdateReq req) {
        if (req.getNickname() != null) nickname = req.getNickname();
        if (req.getPosition() != null) position = req.getPosition();
        if (req.getPhone() != null) phone = req.getPhone();
    }

    public void updatePassword(String passwordHash) {
        password = passwordHash;
    }
}