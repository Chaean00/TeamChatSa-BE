package com.chaean.teamchatsa.domain.team.converter;

import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TeamLevelConverter implements AttributeConverter<TeamLevel, Integer> {

	@Override
	public Integer convertToDatabaseColumn(TeamLevel attribute) {
		return attribute == null ? null : attribute.getValue();
	}

	@Override
	public TeamLevel convertToEntityAttribute(Integer dbData) {
		return dbData == null ? null : TeamLevel.fromValue(dbData);
	}
}
