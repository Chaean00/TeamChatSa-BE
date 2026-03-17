package com.chaean.teamchatsa.infra.ai.service;

import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.infra.ai.dto.MatchSearchIntent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSearchIntentParser {

	private static final List<String> SUPPORTED_REGIONS = List.of(
			"서울", "부산", "대구", "인천", "광주", "대전", "울산",
			"세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
	);

	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	public MatchSearchIntent parse(Team myTeam, String query) {
		try {
			// 내 팀 정보와 검색어를 함께 전달해 검색 의도를 JSON으로 추출
			String prompt = """
					너는 풋살 매치 검색어를 분석해 DB 검색용 JSON만 반환하는 파서다.
					설명 문장, 코드블록, 마크다운 없이 JSON만 출력해라.

					레벨 척도:
					1=하하, 2=하, 3=중하, 4=중, 5=중상, 6=상, 7=상상

					반환 JSON 형식:
					{
					  "levelIn": [정수 배열],
					  "winRateMin": 0.0,
					  "winRateMax": 100.0,
					  "region": "서울 또는 null",
					  "vectorKeyword": "스타일/매너 관련 키워드"
					}

					규칙:
					1. levelIn은 반드시 1~7 정수만 포함해라.
					2. 승률은 0~100 범위의 숫자로 반환해라.
					3. 지역은 서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주 중 하나만 반환하거나 없으면 null로 둬라.
					4. 벡터 검색용 의미어만 vectorKeyword에 담고, 레벨/승률/지역 조건은 별도 필드로 분리해라.
					5. "잘하지 못하는" 같은 부정형 문맥을 정확히 해석해라.
					6. 비슷한 수준이면 levelIn은 내 팀 레벨 기준 ±1 범위, 승률은 ±10 범위를 기본값으로 사용해라.
					7. 높은 수준이면 levelIn은 내 팀보다 높은 레벨 위주, 승률은 내 팀 승률 이상으로 사용해라.
					8. 낮은 수준이면 levelIn은 내 팀보다 낮은 레벨 위주, 승률은 내 팀 승률 이하로 사용해라.

					내 팀 정보:
					- level: %d
					- winRate: %.1f
					- area: %s
					- description: %s

					사용자 검색어:
					%s
					""".formatted(
					myTeam.getLevel().getValue(),
					myTeam.getWinRate(),
					myTeam.getArea(),
					myTeam.getDescription() == null ? "" : myTeam.getDescription(),
					query
			);

			String response = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
			MatchSearchIntent intent = objectMapper.readValue(response, MatchSearchIntent.class);
			return sanitize(intent, myTeam, query);
		} catch (Exception e) {
			log.warn("[AI] 검색 의도 파싱 실패로 fallback 규칙을 사용합니다. query={}", query, e);
			return fallback(myTeam, query);
		}
	}

	// LLM 응답을 현재 서비스 규칙에 맞게 보정
	private MatchSearchIntent sanitize(MatchSearchIntent intent, Team myTeam, String query) {
		MatchSearchIntent sanitized = new MatchSearchIntent();
		sanitized.setLevelIn(sanitizeLevels(intent.getLevelIn(), myTeam, query));
		sanitized.setWinRateMin(sanitizeWinRateMin(intent.getWinRateMin(), myTeam));
		sanitized.setWinRateMax(sanitizeWinRateMax(intent.getWinRateMax(), myTeam));
		sanitized.setRegion(sanitizeRegion(intent.getRegion(), myTeam.getArea()));
		sanitized.setVectorKeyword(sanitizeVectorKeyword(intent.getVectorKeyword(), query));
		return sanitized;
	}

	private List<Integer> sanitizeLevels(List<Integer> levels, Team myTeam, String query) {
		List<Integer> filtered = levels == null ? List.of() : levels.stream()
				.filter(level -> level != null && level >= 1 && level <= 7)
				.distinct()
				.sorted()
				.toList();

		if (!filtered.isEmpty()) {
			return filtered;
		}

		return fallback(myTeam, query).getLevelIn();
	}

	private Double sanitizeWinRateMin(Double winRateMin, Team myTeam) {
		if (winRateMin == null) {
			return Math.max(0.0, myTeam.getWinRate() - 10.0);
		}
		return Math.max(0.0, Math.min(100.0, winRateMin));
	}

	private Double sanitizeWinRateMax(Double winRateMax, Team myTeam) {
		if (winRateMax == null) {
			return Math.min(100.0, myTeam.getWinRate() + 10.0);
		}
		return Math.max(0.0, Math.min(100.0, winRateMax));
	}

	private String sanitizeRegion(String region, String teamArea) {
		if (region != null && SUPPORTED_REGIONS.contains(region)) {
			return region;
		}

		if (teamArea == null || teamArea.isBlank()) {
			return null;
		}

		for (String supportedRegion : SUPPORTED_REGIONS) {
			if (teamArea.startsWith(supportedRegion)) {
				return supportedRegion;
			}
		}

		return teamArea.split(" ")[0];
	}

	private String sanitizeVectorKeyword(String vectorKeyword, String query) {
		if (vectorKeyword == null || vectorKeyword.isBlank()) {
			return query.trim();
		}
		return vectorKeyword.trim();
	}

	// LLM 응답이 깨질 때 사용할 최소 규칙 기반 fallback
	private MatchSearchIntent fallback(Team myTeam, String query) {
		String normalizedQuery = query.toLowerCase(Locale.ROOT);
		MatchSearchIntent intent = new MatchSearchIntent();
		intent.setRegion(sanitizeRegion(extractRegion(query), myTeam.getArea()));
		intent.setVectorKeyword(query.trim());

		if (containsAny(normalizedQuery, "조금 잘", "우리보다 잘", "더 잘", "강한", "센", "상위")) {
			intent.setLevelIn(range(Math.min(7, myTeam.getLevel().getValue() + 1), 7));
			intent.setWinRateMin(myTeam.getWinRate());
			intent.setWinRateMax(100.0);
			return intent;
		}

		if (containsAny(normalizedQuery, "조금 못", "우리보다 약", "약한", "쉬운", "하위")) {
			intent.setLevelIn(range(1, Math.max(1, myTeam.getLevel().getValue() - 1)));
			intent.setWinRateMin(0.0);
			intent.setWinRateMax(myTeam.getWinRate());
			return intent;
		}

		intent.setLevelIn(range(
				Math.max(1, myTeam.getLevel().getValue() - 1),
				Math.min(7, myTeam.getLevel().getValue() + 1)
		));
		intent.setWinRateMin(Math.max(0.0, myTeam.getWinRate() - 10.0));
		intent.setWinRateMax(Math.min(100.0, myTeam.getWinRate() + 10.0));
		return intent;
	}

	private String extractRegion(String query) {
		for (String region : SUPPORTED_REGIONS) {
			if (query.contains(region)) {
				return region;
			}
		}
		return null;
	}

	private boolean containsAny(String query, String... keywords) {
		for (String keyword : keywords) {
			if (query.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private List<Integer> range(int startInclusive, int endInclusive) {
		return IntStream.rangeClosed(startInclusive, endInclusive)
				.boxed()
				.toList();
	}
}
