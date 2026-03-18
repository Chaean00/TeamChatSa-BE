package com.chaean.teamchatsa.infra.ai.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MatchSearchIntent {

	private List<Integer> levelIn;
	private Double winRateMin;
	private Double winRateMax;
	private String region;
	private String vectorKeyword;
}
