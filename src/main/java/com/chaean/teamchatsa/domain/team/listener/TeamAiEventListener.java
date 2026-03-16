package com.chaean.teamchatsa.domain.team.listener;

import com.chaean.teamchatsa.domain.team.event.TeamReviewCreatedEvent;
import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamReview;
import com.chaean.teamchatsa.domain.team.repository.TeamRepository;
import com.chaean.teamchatsa.domain.team.repository.TeamReviewRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamAiEventListener {

	private final TeamRepository teamRepository;
	private final TeamReviewRepository teamReviewRepository;
	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;

	/**
	 * 팀 리뷰 생성 시 AI 스타일 벡터 갱신 이벤트 핸들러
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional
	public void handleTeamAiEvent(TeamReviewCreatedEvent event) {
		Long teamId = event.getTeamId();
		log.info("[AI] 팀 스타일 벡터 갱신 시작 - TeamId: {}", teamId);

		// 팀 최신 리뷰 데이터 수집 (최대 10개)
		List<TeamReview> reviews = teamReviewRepository.findTop10ByTeamIdOrderByCreatedAtDesc(teamId);
		if (reviews.isEmpty()) {
			log.warn("[AI] 팀 리뷰가 존재하지 않아 벡터를 갱신할 수 없습니다. TeamId: {}", teamId);
			return;
		}

		String combinedReviews = reviews.stream()
				.map(TeamReview::getContent)
				.collect(Collectors.joining(". "));

		// Gemini ChatModel을 이용한 플레이 스타일 요약
		String prompt = String.format("""
				다음은 특정 팀에 대한 여러 사용자들의 리뷰 내용입니다:
				[%s]
				
				위 리뷰들을 바탕으로 이 팀의 전반적인 플레이 스타일과 분위기를 '딱 두 문장'으로 요약해 주세요.
				팀의 실력, 매너, 분위기 등 핵심 특징이 잘 드러나야 합니다.
				예시: "이 팀은 뛰어난 조직력을 바탕으로 공격적인 플레이를 즐기는 팀입니다. 매너가 매우 좋아 상대 팀을 배려하며 전반적으로 즐겁게 게임하는 분위기입니다."
				""", combinedReviews);

		String summary = chatModel.call(prompt);
		log.debug("[AI] 팀 스타일 요약 완료: {}", summary);

		// 요약된 문장을 512차원 벡터로 변환 (Embedding)
		float[] styleVector = embeddingModel.embed(summary);
		log.debug("[AI] 임베딩 벡터 생성 완료 (Dimension: {})", styleVector.length);

		// 팀 엔티티에 스타일 벡터 업데이트
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다. ID: " + teamId));

		team.updateStyleVector(styleVector);
		teamRepository.save(team);

		log.info("[AI] 팀 스타일 벡터 갱신 완료 - TeamId: {}", teamId);
	}
}
