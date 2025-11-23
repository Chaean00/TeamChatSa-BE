package com.chaean.teamchatsa.domain.match.repository.projection;

import java.time.LocalDateTime;

/** 위치 기반 매치 검색 결과 프로젝션 */
public interface MatchLocationProjection {
	Long getId();
	String getTitle();
	LocalDateTime getMatchDate();
	String getTeamName();
	String getLevel();
	Double getLat();
	Double getLng();
}