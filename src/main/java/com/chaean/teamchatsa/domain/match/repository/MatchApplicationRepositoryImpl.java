package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.dto.response.MatchApplicantResponse;
import com.chaean.teamchatsa.domain.match.model.QMatchApplication;
import com.chaean.teamchatsa.domain.team.model.QTeam;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MatchApplication QueryDSL 구현체 - 네이밍: MatchApplicationRepositoryImpl (Spring이 자동 인식) - @Repository 필수
 */
@Repository
@RequiredArgsConstructor
public class MatchApplicationRepositoryImpl implements MatchApplicationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<MatchApplicantResponse> findApplicantsByMatchIdWithTeamInfo(Long matchId) {
		QMatchApplication ma = QMatchApplication.matchApplication;
		QTeam t = QTeam.team;

		return queryFactory
				.select(Projections.constructor(
						MatchApplicantResponse.class,
						ma.id,
						t.id,
						t.name,
						t.img,
						t.level,
						ma.message,
						ma.createdAt,
						ma.status
				))
				.from(ma)
				.join(t).on(ma.applicantTeamId.eq(t.id))
				.where(ma.postId.eq(matchId))
				.orderBy(ma.createdAt.desc())
				.fetch();
	}
}
