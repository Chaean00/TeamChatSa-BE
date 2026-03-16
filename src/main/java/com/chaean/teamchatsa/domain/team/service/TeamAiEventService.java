package com.chaean.teamchatsa.domain.team.service;

import com.chaean.teamchatsa.domain.team.event.TeamReviewCreatedEvent;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamReview;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamReviewRepository;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamAiEventService {

	private final TeamRepository teamRepository;
	private final TeamReviewRepository teamReviewRepository;
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;

	@Transactional
	public void handleTeamAiEvent(TeamReviewCreatedEvent event) {
		Long teamId = event.getTeamId();
		log.info("[AI] 팀 스타일 벡터 갱신 시작 - TeamId: {}", teamId);

		// 최신 팀 리뷰 조회
		List<TeamReview> reviews = teamReviewRepository.findTop10ByTeamIdOrderByCreatedAtDesc(teamId);
		if (reviews.isEmpty()) {
			log.warn("[AI] 팀 리뷰가 존재하지 않아 벡터를 갱신할 수 없습니다. TeamId: {}", teamId);
			return;
		}

		String combinedReviews = reviews.stream()
				.map(TeamReview::getContent)
				.reduce((left, right) -> left + ". " + right)
				.orElse("");

		// AI 요약용 프롬프트 생성
		String prompt = String.format("""
				다음은 특정 팀에 대한 여러 사용자들의 리뷰 내용입니다:
				[%s]
				
				위 리뷰들을 바탕으로 이 팀의 전반적인 플레이 스타일과 분위기를 '딱 두 문장'으로 요약해 주세요.
				팀의 실력, 매너, 분위기 등 핵심 특징이 잘 드러나야 합니다.
				예시: "이 팀은 뛰어난 조직력을 바탕으로 공격적인 플레이를 즐기는 팀입니다. 매너가 매우 좋아 상대 팀을 배려하며 전반적으로 즐겁게 게임하는 분위기입니다."
				""", combinedReviews);

		// 팀 스타일 요약 생성
		String summary = chatModel.call(prompt);
		log.debug("[AI] 팀 스타일 요약 완료: {}", summary);

		// 팀 스타일 임베딩 벡터 생성
		float[] styleVector = embeddingModel.embed(summary);
		log.debug("[AI] 임베딩 벡터 생성 완료 (Dimension: {})", styleVector.length);

		// 스타일 벡터 반영 대상 팀 조회
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND);

		// 팀 엔티티 스타일 벡터 갱신
		team.updateStyleVector(styleVector);

		// 갱신된 팀 스타일 벡터 저장
		teamRepository.save(team);

		log.info("[AI] 팀 스타일 벡터 갱신 완료 - TeamId: {}", teamId);
	}
}
