package com.chaean.teamchatsa.domain.team.repository;

import com.chaean.teamchatsa.domain.team.dto.response.TeamListRes;
import com.chaean.teamchatsa.domain.team.model.QTeam;
import com.chaean.teamchatsa.domain.team.model.QTeamMember;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Team QueryDSL 구현체
 * - 네이밍: TeamRepositoryImpl (Spring이 자동 인식)
 * - @Repository 필수
 */
@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	/**
	 * 팀 목록 조회 (QueryDSL 최적화)
	 * LEFT JOIN + GROUP BY 방식으로 서브쿼리 제거
	 */
	@Override
	public Slice<TeamListRes> findTeamListWithPagination(Pageable pageable) {
		QTeam t = QTeam.team;
		QTeamMember tm = QTeamMember.teamMember;

		List<TeamListRes> content = queryFactory
				.select(Projections.constructor(
						TeamListRes.class,
						t.id,
						t.name,
						t.area,
						t.img,
						t.description,
						tm.id.count()
				))
				.from(t)
				.leftJoin(tm).on(
						tm.teamId.eq(t.id)
							.and(tm.isDeleted.eq(false))
				)
				.where(t.isDeleted.eq(false))
				.groupBy(t.id, t.name, t.area, t.img, t.description, t.createdAt)
				.orderBy(t.createdAt.desc(), t.id.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize() + 1)
				.fetch();

		// Slice 생성 (무한 스크롤)
		return createSlice(content, pageable);
	}

	private <T> Slice<T> createSlice(List<T> content, Pageable pageable) {
		boolean hasNext = false;
		if (content.size() > pageable.getPageSize()) {
			content.remove(pageable.getPageSize());
			hasNext = true;
		}
		return new SliceImpl<>(content, pageable, hasNext);
	}
}
