package com.chaean.teamchatsa.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		String jwt = "JWT";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
		Components components = new Components()
				.addSecuritySchemes(jwt, new SecurityScheme()
						.name(jwt)
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER)
						.description("JWT 토큰을 입력해주세요 (Bearer 제외)")
				);

		return new OpenAPI()
				.addServersItem(new Server().url("/").description("Default Server"))
				.addSecurityItem(securityRequirement)
				.components(components)
				.info(new Info()
						.title("팀찾사 API 문서")
						.description("아마추어 축구/풋살 팀 매칭 서비스 API 문서")
						.version("v1.0.0")
				);
	}
}
