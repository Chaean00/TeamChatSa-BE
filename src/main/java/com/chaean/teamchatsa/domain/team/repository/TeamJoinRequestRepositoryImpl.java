package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamApplicationRes;
import com.chaean.teamchatsa.domain.team.model.JoinStatus;
import com.chaean.teamchatsa.domain.team.model.QTeamApplication;
import com.chaean.teamchatsa.domain.team.model.TeamApplication;
import com.chaean.teamchatsa.domain.user.model.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TeamJoinRequest QueryDSL 구현체
 * - 네이밍: TeamJoinRequestRepositoryImpl (Spring이 자동 인식)
 * - @Repository 필수
 */
@Repository
@RequiredArgsConstructor
public class TeamJoinRequestRepositoryImpl implements TeamJoinRequestRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<TeamApplicationRes> findApplicationsByTeamId(Long teamId) {
		QTeamApplication ta = QTeamApplication.teamApplication;
		QUser u = QUser.user;

		return queryFactory
				.select(Projections.constructor(
						TeamApplicationRes.class,
						ta.id,
						u.id,
						u.username,
						u.nickname,
						ta.message,
						ta.status,
						ta.createdAt
				))
				.from(ta)
				.join(u).on(ta.userId.eq(u.id).and(u.isDeleted.eq(false)))
				.where(ta.teamId.eq(teamId))
				.orderBy(ta.createdAt.desc())
				.fetch();
	}

	@Override
	public List<TeamApplicationRes> findApplicationsByTeamIdAndStatus(Long teamId, JoinStatus status) {
		QTeamApplication ta = QTeamApplication.teamApplication;
		QUser u = QUser.user;

		return queryFactory
				.select(Projections.constructor(
						TeamApplicationRes.class,
						ta.id,
						u.id,
						u.username,
						u.nickname,
						ta.message,
						ta.status,
						ta.createdAt
				))
				.from(ta)
				.join(u).on(ta.userId.eq(u.id).and(u.isDeleted.eq(false)))
				.where(
						ta.teamId.eq(teamId)
								.and(ta.status.eq(status))
				)
				.orderBy(ta.createdAt.desc())
				.fetch();
	}

	@Override
	public List<TeamApplication> findPendingApplicationsByUserIdExcluding(Long userId, Long excludeApplicationId) {
		QTeamApplication ta = QTeamApplication.teamApplication;

		return queryFactory
				.selectFrom(ta)
				.where(
						ta.userId.eq(userId)
								.and(ta.status.eq(JoinStatus.PENDING))
								.and(ta.id.ne(excludeApplicationId))
				)
				.fetch();
	}
}