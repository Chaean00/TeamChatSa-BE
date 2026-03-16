package com.chaean.teamchatsa.domain.match.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_post")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE match_post SET deleted_at = NOW() WHERE id = ?")
public class MatchPost extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@NotNull
	@Column(name = "team_id", nullable = false)
	private Long teamId;

	@Column(name = "accepted_application_id")
	private Long acceptedApplicationId;

	@Size(max = 100)
	@NotNull
	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@NotNull
	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@NotNull
	@Column(name = "head_count", nullable = false)
	private int headCount;

	@NotNull
	@Column(name = "match_date", nullable = false)
	private LocalDateTime matchDate;

	@NotNull
	@Column(name = "lat", nullable = false)
	private Double lat;

	@NotNull
	@Column(name = "lng", nullable = false)
	private Double lng;

	@Column(name = "location", columnDefinition = "GEOMETRY(Point,4326)", insertable = false, updatable = false)
	private String location; // DB에서 자동 생성되는 컬럼

	@NotNull
	@Column(name = "address", nullable = false, length = 255)
	private String address;

	@Size(max = 120)
	@Column(name = "place_name", length = 120)
	private String placeName;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private MatchPostStatus status = MatchPostStatus.OPEN;

	public static MatchPost of(Long teamId, String title, String content, int headCount,
			LocalDateTime matchDate, Double lat, Double lng, String address, String placeName) {
		MatchPost post = new MatchPost();
		post.teamId = teamId;
		post.title = title;
		post.content = content;
		post.headCount = headCount;
		post.matchDate = matchDate;
		post.lat = lat;
		post.lng = lng;
		post.address = address;
		post.placeName = placeName;
		return post;
	}

	public void updateStatus(MatchPostStatus status) {
		if (this.status == status) {
			return; // 동일 상태면 무시
		}
		if (this.status == MatchPostStatus.CLOSED && status == MatchPostStatus.OPEN) {
			throw new IllegalStateException("마감된 매치는 다시 열 수 없습니다.");
		}
		this.status = status;
	}

	public void updateOpponent(Long acceptedApplicationId) {
		this.acceptedApplicationId = acceptedApplicationId;
	}
}