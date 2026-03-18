package com.seo92js.news_alpha_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NewsAlphaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsAlphaBackendApplication.class, args);
	}

}
