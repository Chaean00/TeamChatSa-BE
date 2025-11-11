package com.chaean.teamchatsa.global.common.aop.annotation;

import com.chaean.teamchatsa.domain.team.model.TeamRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 특정 TeamRole을 가진 사용자만 접근할 수 있도록 제한하는 어노테이션.
 * 메서드의 파라미터 중 teamId를 통해 사용자의 팀 권한을 검증.
 * userId는 파라미터에서 찾지 못하면 SecurityContext에서 자동으로 추출.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTeamRole {
    /** 접근 가능한 팀 역할 목록 - TeamRole 배열 */
    TeamRole[] value();

    /** teamId 파라미터의 이름 - 기본값: "teamId") */
    String teamIdParam() default "teamId";

    /** userId 파라미터의 이름 - 기본값: "userId" */
    String userIdParam() default "userId";
}
