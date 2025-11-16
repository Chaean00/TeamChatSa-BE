package com.chaean.teamchatsa.domain.match.repository;

import com.chaean.teamchatsa.domain.match.dto.response.MatchPostDetailRes;
import com.chaean.teamchatsa.domain.match.dto.response.MatchPostListRes;
import com.chaean.teamchatsa.domain.match.model.QMatchPost;
import com.chaean.teamchatsa.domain.team.model.QTeam;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MatchPost QueryDSL 구현체
 * - 네이밍: MatchPostRepositoryImpl (Spring이 자동 인식)
 * - @Repository 필수
 */
@Repository
@RequiredArgsConstructor
public class MatchPostRepositoryImpl implements MatchPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<MatchPostListRes> findMatchPostListWithPagination(Pageable pageable) {
		QMatchPost mp = QMatchPost.matchPost;
		QTeam t = QTeam.team;

		List<MatchPostListRes> content = queryFactory
				.select(Projections.constructor(
						MatchPostListRes.class,
						mp.id,
						mp.title,
						mp.placeName,
						mp.matchDate,
						t.name,
						mp.address,
						mp.status,
						t.level
				))
				.from(mp)
				.leftJoin(t).on(t.id.eq(mp.teamId).and(t.isDeleted.eq(false)))
				.where(mp.isDeleted.eq(false))
				.orderBy(mp.createdAt.desc(), mp.id.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize() + 1)  // hasNext 판단용
				.fetch();

		// Slice 생성 (무한 스크롤)
		return createSlice(content, pageable);
	}

	@Override
	public Slice<MatchPostListRes> findMatchPostListByTeamId(Long teamId, Pageable pageable) {
		QMatchPost mp = QMatchPost.matchPost;
		QTeam t = QTeam.team;

		List<MatchPostListRes> content = queryFactory
				.select(Projections.constructor(
						MatchPostListRes.class,
						mp.id,
						mp.title,
						mp.placeName,
						mp.matchDate,
						t.name,
						mp.address,
						mp.status,
						t.level
				))
				.from(mp)
				.leftJoin(t).on(t.id.eq(mp.teamId).and(t.isDeleted.eq(false)))
				.where(
						mp.teamId.eq(teamId)
								.and(mp.isDeleted.eq(false))
				)
				.orderBy(mp.createdAt.desc(), mp.id.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize() + 1)  // hasNext 판단용
				.fetch();

		// Slice 생성 (무한 스크롤)
		return createSlice(content, pageable);
	}

	@Override
	public MatchPostDetailRes findMatchPostDetailById(Long matchId) {
		QMatchPost mp = QMatchPost.matchPost;
		QTeam t = QTeam.team;

		MatchPostDetailRes content = queryFactory
				.select(Projections.constructor(
						MatchPostDetailRes.class,
						mp.id,
						mp.teamId,
						mp.title,
						mp.content,
						mp.placeName,
						mp.address,
						mp.lat,
						mp.lng,
						mp.matchDate,
						t.name,
						t.img,
						t.level
				))
				.from(mp)
				.leftJoin(t).on(
						t.id.eq(mp.teamId)
								.and(t.isDeleted.eq(false))
				)
				.where(mp.id.eq(matchId)
						.and(mp.isDeleted.eq(false)))
				.fetchOne();

		return content;
	}

	/** Slice 생성 헬퍼 메서드 */
	private <T> Slice<T> createSlice(List<T> content, Pageable pageable) {
		boolean hasNext = false;
		if (content.size() > pageable.getPageSize()) {
			content.remove(pageable.getPageSize());
			hasNext = true;
		}
		return new SliceImpl<>(content, pageable, hasNext);
	}
}
