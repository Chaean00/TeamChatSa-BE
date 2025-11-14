package com.chaean.teamchatsa.global.common.aop.annotation;

import com.chaean.teamchatsa.domain.team.model.TeamRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 특정 TeamRole을 가진 사용자만 접근할 수 있도록 제한하는 어노테이션. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTeamRole {
	/** 접근 가능한 팀 역할 목록 - TeamRole 배열 */
	TeamRole[] value();
}
