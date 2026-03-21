package com.seo92js.news_alpha_backend;

import com.seo92js.news_alpha_backend.domain.news.NaverNewsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableConfigurationProperties(NaverNewsProperties.class)
@SpringBootApplication
public class NewsAlphaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsAlphaBackendApplication.class, args);
	}

}
