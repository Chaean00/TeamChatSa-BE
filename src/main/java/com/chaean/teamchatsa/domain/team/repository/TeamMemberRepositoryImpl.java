package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamMemberRes;
import com.chaean.teamchatsa.domain.team.model.QTeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.user.model.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryImpl implements TeamMemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<TeamMemberRes> findTeamMembersByTeamId(Long teamId) {
		QTeamMember tm = QTeamMember.teamMember;
		QUser u = QUser.user;

		NumberExpression<Integer> roleOrder = new CaseBuilder()
				.when(tm.role.eq(TeamRole.LEADER)).then(1)
				.when(tm.role.eq(TeamRole.CO_LEADER)).then(2)
				.otherwise(3);

		return queryFactory
				.select(Projections.constructor(
						TeamMemberRes.class,
						tm.userId,
						u.username,
						u.nickname,
						u.email,
						u.position,
						tm.role,
						tm.createdAt
				))
				.from(tm)
				.leftJoin(u).on(
						tm.userId.eq(u.id)
				)
				.where(
						tm.teamId.eq(teamId),
						tm.isDeleted.eq(false)
				)
				.orderBy(
						roleOrder.asc(),
						tm.createdAt.asc()
				)
				.fetch();
	}
}
