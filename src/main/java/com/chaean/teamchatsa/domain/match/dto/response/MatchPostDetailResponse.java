package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MatchPostDetailResponse {

	private Long postId;
	private Long teamId;
	private String title;
	private String content;
	private String placeName;
	private String address;
	private double lat;
	private double lng;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private String teamImg;
	private Integer teamLevel;
	private String teamLevelLabel;
	private Boolean contactVisible;
	private String myTeamName;
	private String myContactType;
	private String myContact;
	private String opponentTeamName;
	private String opponentContactType;
	private String opponentContact;

	@Builder(access = AccessLevel.PRIVATE)
	private MatchPostDetailResponse(Long postId,
			Long teamId,
			String title,
			String content,
			String placeName,
			String address,
			double lat,
			double lng,
			LocalDate matchDate,
			LocalTime matchTime,
			String teamName,
			String teamImg,
			Integer teamLevel,
			String teamLevelLabel,
			Boolean contactVisible,
			String myTeamName,
			String myContactType,
			String myContact,
			String opponentTeamName,
			String opponentContactType,
			String opponentContact) {
		this.postId = postId;
		this.teamId = teamId;
		this.title = title;
		this.content = content;
		this.placeName = placeName;
		this.address = address;
		this.lat = lat;
		this.lng = lng;
		this.matchDate = matchDate;
		this.matchTime = matchTime;
		this.teamName = teamName;
		this.teamImg = teamImg;
		this.teamLevel = teamLevel;
		this.teamLevelLabel = teamLevelLabel;
		this.contactVisible = contactVisible;
		this.myTeamName = myTeamName;
		this.myContactType = myContactType;
		this.myContact = myContact;
		this.opponentTeamName = opponentTeamName;
		this.opponentContactType = opponentContactType;
		this.opponentContact = opponentContact;
	}

	public MatchPostDetailResponse(Long postId,
			Long teamId,
			String title,
			String content,
			String placeName,
			String address,
			double lat,
			double lng,
			LocalDateTime matchDateTime,
			String teamName,
			String teamImg,
			TeamLevel teamLevel) {
		this(
				postId,
				teamId,
				title,
				content,
				placeName,
				address,
				lat,
				lng,
				matchDateTime.toLocalDate(),
				matchDateTime.toLocalTime(),
				teamName,
				teamImg,
				teamLevel != null ? teamLevel.getValue() : null,
				teamLevel != null ? teamLevel.getDescription() : null,
				false,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	public static MatchPostDetailResponse of(Long postId,
			Long teamId,
			String title,
			String content,
			String placeName,
			String address,
			double lat,
			double lng,
			LocalDateTime matchDateTime,
			String teamName,
			String teamImg,
			TeamLevel teamLevel) {
		return MatchPostDetailResponse.builder()
				.postId(postId)
				.teamId(teamId)
				.title(title)
				.content(content)
				.placeName(placeName)
				.address(address)
				.lat(lat)
				.lng(lng)
				.matchDate(matchDateTime.toLocalDate())
				.matchTime(matchDateTime.toLocalTime())
				.teamName(teamName)
				.teamImg(teamImg)
				.teamLevel(teamLevel != null ? teamLevel.getValue() : null)
				.teamLevelLabel(teamLevel != null ? teamLevel.getDescription() : null)
				.contactVisible(false)
				.build();
	}

	public static MatchPostDetailResponse withContactInfo(MatchPostDetailResponse base, Team myTeam, Team opponentTeam) {
		return MatchPostDetailResponse.builder()
				.postId(base.postId)
				.teamId(base.teamId)
				.title(base.title)
				.content(base.content)
				.placeName(base.placeName)
				.address(base.address)
				.lat(base.lat)
				.lng(base.lng)
				.matchDate(base.matchDate)
				.matchTime(base.matchTime)
				.teamName(base.teamName)
				.teamImg(base.teamImg)
				.teamLevel(base.teamLevel)
				.teamLevelLabel(base.teamLevelLabel)
				.contactVisible(true)
				.myTeamName(myTeam.getName())
				.myContactType(myTeam.getContactType().name())
				.myContact(myTeam.getContact())
				.opponentTeamName(opponentTeam.getName())
				.opponentContactType(opponentTeam.getContactType().name())
				.opponentContact(opponentTeam.getContact())
				.build();
	}
}
