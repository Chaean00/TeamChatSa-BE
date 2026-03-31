package com.chaean.teamchatsa.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final String uploadBaseDir;

	public WebConfig(@Value("${app.upload.base-dir:./uploads}") String uploadBaseDir) {
		this.uploadBaseDir = uploadBaseDir;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/v1/**")
				.allowedOrigins("http://localhost:3000")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
				.allowedHeaders("Authorization", "Content-Type")
				.allowCredentials(true);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String location = "file:" + uploadBaseDir;
		if (!uploadBaseDir.endsWith("/")) {
			location += "/";
		}

		registry.addResourceHandler("/api/v1/uploads/files/**")
				.addResourceLocations(location);
	}
}
