package com.chaean.teamchatsa.domain.match.repository.projection;

import java.time.LocalDateTime;

/**
 * 위치 기반 매치 검색 결과 프로젝션
 * Native Query 결과를 타입 안전하게 매핑
 */
public interface MatchLocationProjection {
	Long getId();
	String getTitle();
	String getPlaceName();
	LocalDateTime getMatchDate();
	String getTeamName();
	String getAddress();
	String getStatus();
	String getLevel();
	Double getLat();
	Double getLng();
	Double getDistance();
}