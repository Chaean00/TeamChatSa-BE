package com.chaean.teamchatsa.global.common.aop.aspect;

import com.chaean.teamchatsa.domain.team.model.TeamMember;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import com.chaean.teamchatsa.domain.team.repository.TeamMemberRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.RequireTeamRole;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @RequireTeamRole 어노테이션이 붙은 메서드에 대한 팀 역할 기반 접근 제어를 처리하는 Aspect입니다.
 * SecurityContext에서 userId를 추출하여 사용자의 팀 권한을 검증합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TeamRoleAspect {

	private final TeamMemberRepository teamMemberRepository;

	/**
	 * @RequireTeamRole 어노테이션이 붙은 메서드 실행 전에 팀 역할을 검증
	 * userId는 파라미터에서 찾지 못하면 SecurityContext에서 자동으로 추출
	 */
	@Before("@annotation(requireTeamRole)")
	public void checkTeamRole(RequireTeamRole requireTeamRole) {
		Long userId = getUserIdFromSecurityContext();

		// userId 기준으로 팀 멤버 조회 (전제: 한 유저는 하나의 팀만 가진다)
		TeamMember teamMember = teamMemberRepository
				.findByUserIdAndIsDeletedFalse(userId)
				.orElseThrow(() -> {
					log.warn("[권한 검증 실패] 팀에 소속되지 않은 사용자입니다. userId: {}", userId);
					return new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
				});

		TeamRole userRole = teamMember.getRole();
		TeamRole[] requiredRoles = requireTeamRole.value();

		boolean hasRequiredRole = Arrays.stream(requiredRoles)
				.anyMatch(role -> role == userRole);

		if (!hasRequiredRole) {
			log.warn("[팀 권한 검증 실패] 사용자의 역할이 요구 조건을 충족하지 못합니다. " +
							"userId: {}, 현재 역할: {}, 요구 역할: {}",
					userId, userRole, Arrays.toString(requiredRoles));
			throw new BusinessException(ErrorCode.INSUFFICIENT_TEAM_ROLE);
		}

		log.info("[팀 권한 검증 성공] userId: {}, 역할: {}", userId, userRole);
	}

	/** SecurityContext에서 현재 인증된 사용자의 userId를 추출 */
	private Long getUserIdFromSecurityContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			log.error("[팀 권한 검증 실패] SecurityContext에 인증 정보가 없습니다.");
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Long) {
			return (Long) principal;
		}

		log.error("[팀 권한 검증 실패] principal 타입이 지원되지 않습니다. type: {}",
				principal.getClass().getName());
		throw new BusinessException(ErrorCode.UNAUTHORIZED);
	}
}
