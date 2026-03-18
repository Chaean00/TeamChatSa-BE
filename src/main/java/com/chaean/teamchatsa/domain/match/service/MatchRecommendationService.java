package com.chaean.teamchatsa.domain.match.service;

import com.chaean.teamchatsa.domain.match.dto.request.MatchRecommendationRequest;
import com.chaean.teamchatsa.domain.match.dto.response.MatchRecommendationCandidate;
import com.chaean.teamchatsa.domain.match.dto.response.MatchRecommendationResponse;
import com.chaean.teamchatsa.domain.match.repository.MatchPostRepository;
import com.chaean.teamchatsa.domain.match.repository.projection.MatchRecommendationProjection;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import com.chaean.teamchatsa.infra.ai.dto.MatchSearchIntent;
import com.chaean.teamchatsa.infra.ai.service.MatchSearchIntentParser;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchRecommendationService {

	private final TeamMemberRepository teamMemberRepository;
	private final TeamRepository teamRepository;
	private final MatchPostRepository matchPostRepository;
	private final EmbeddingModel embeddingModel;
	private final MatchSearchIntentParser matchSearchIntentParser;

	public List<MatchRecommendationResponse> recommendMatches(Long userId, MatchRecommendationRequest req) {
		// 추천 기준이 되는 사용자 팀을 조회
		Long myTeamId = teamMemberRepository.findTeamIdByUserId(userId);
		if (myTeamId == null) {
			throw new BusinessException(ErrorCode.TEAM_NOT_FOUND, "소속된 팀이 없어 매치를 추천할 수 없습니다.");
		}

		// 사용자 팀의 스타일과 지역 정보를 조회
		Team myTeam = teamRepository.findById(myTeamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

		// 공백을 정리한 검색어를 AI 파서에 전달
		String normalizedQuery = req.getQuery().trim();
		MatchSearchIntent intent = matchSearchIntentParser.parse(myTeam, normalizedQuery);

		// AI가 추출한 스타일 키워드만 임베딩으로 변환
		float[] queryVector = createQueryEmbedding(intent.getVectorKeyword());

		// exact filter와 vector search를 결합해 추천 후보를 조회
		List<MatchRecommendationCandidate> candidates = matchPostRepository.findRecommendedMatches(
						myTeamId,
						toVectorLiteral(queryVector),
						intent.getRegion(),
						intent.getLevelIn().stream().toList(),
						intent.getWinRateMin(),
						intent.getWinRateMax(),
						LocalDateTime.now()
				).stream()
				.map(this::toCandidate)
				.toList();

		log.info("조건 - 지역: {}, 레벨 범위: {}, 승률 범위: {}~{}", intent.getRegion(), intent.getLevelIn(), intent.getWinRateMin(),
				intent.getWinRateMax());
		log.info("추천 후보 {}건 조회", candidates.size());

		// 추천 결과에 AI 해석을 반영한 설명 추가
		return candidates.stream()
				.map(candidate -> MatchRecommendationResponse.of(candidate, buildReason(myTeam, candidate, intent)))
				.toList();
	}

	// 추천 결과에 지역과 실력 차이를 반영한 설명을 생성
	private String buildReason(Team myTeam, MatchRecommendationCandidate candidate, MatchSearchIntent intent) {
		String levelDescription = describeLevelGap(candidate.getTeamLevel().getValue() - myTeam.getLevel().getValue());
		String regionDescription = intent.getRegion() == null ? "원하는 조건" : intent.getRegion() + " 지역 조건";

		return "%s에 맞고, %s 상대 팀의 스타일 키워드와 유사한 마감되지 않은 매치입니다.".formatted(regionDescription, levelDescription);
	}

	private String describeLevelGap(int gap) {
		if (gap >= 2) {
			return "우리 팀보다 확실히 높은 수준의";
		}

		if (gap == 1) {
			return "우리 팀보다 조금 높은 수준의";
		}

		if (gap == 0) {
			return "우리 팀과 비슷한 수준의";
		}

		if (gap == -1) {
			return "우리 팀보다 조금 낮은 수준의";
		}

		return "우리 팀보다 확실히 낮은 수준의";
	}

	private MatchRecommendationCandidate toCandidate(MatchRecommendationProjection projection) {
		return new MatchRecommendationCandidate(
				projection.getMatchId(),
				projection.getMatchTitle(),
				projection.getPlaceName(),
				projection.getMatchDateTime(),
				projection.getTeamId(),
				projection.getTeamName(),
				projection.getMatchAddress(),
				TeamLevel.fromValue(projection.getTeamLevel())
		);
	}

	private String toVectorLiteral(float[] vector) {
		StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < vector.length; i++) {
			if (i > 0) {
				builder.append(',');
			}
			builder.append(vector[i]);
		}
		builder.append(']');
		return builder.toString();
	}

	private float[] createQueryEmbedding(String keyword) {
		try {
			float[] vector = embeddingModel.embed(keyword);
			log.info("AI 키워드: {}, vector dimension: {}", keyword, vector.length);

			return vector;
		} catch (Exception e) {
			log.error("임베딩 생성 실패", e);
			throw new BusinessException(ErrorCode.EMBEDDING_FAILURE, "검색어 임베딩 생성에 실패했습니다.");
		}
	}
}
