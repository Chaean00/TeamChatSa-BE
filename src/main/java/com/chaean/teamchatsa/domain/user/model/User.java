package com.chaean.teamchatsa.domain.user.model;

import com.chaean.teamchatsa.domain.team.model.Position;
import com.chaean.teamchatsa.domain.user.dto.requset.UserUpdateRequest;
import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "\"user\"", uniqueConstraints = {
		@UniqueConstraint(name = "uc_user_nickname", columnNames = {"nickname"}),
		@UniqueConstraint(name = "uc_user_email", columnNames = {"email"})
})
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = NOW() WHERE id = ?")
public class User extends BaseEntity {

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
	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role = UserRole.USER;

	@NotNull
	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "position", nullable = false)
	private Position position = Position.ALL;

	/**
	 * 신규 회원 가입을 위한 정적 팩토리 메서드
	 */
	public static User create(String username, String email, String password, Position position, String phone) {
		return User.builder()
				.username(username)
				.email(email)
				.password(password)
				.position(position)
				.phone(phone)
				.role(UserRole.USER)
				.build();
	}

	/**
	 * OAuth 연동 등 최소 정보를 이용한 생성 메서드
	 */
	public static User of(String username, String email, String passwordHash) {
		return User.builder()
				.username(username)
				.email(email)
				.password(passwordHash)
				.role(UserRole.USER)
				.position(Position.ALL)
				.build();
	}

	public void update(UserUpdateRequest req) {
		if (req.getNickname() != null) {
			nickname = req.getNickname();
		}
		if (req.getPosition() != null) {
			position = req.getPosition();
		}
		if (req.getPhone() != null) {
			phone = req.getPhone();
		}
	}

	public void updatePassword(String passwordHash) {
		password = passwordHash;
	}
}
