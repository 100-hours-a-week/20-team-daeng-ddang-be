package com.daengddang.daengdong_map;

import com.daengddang.daengdong_map.config.jwt.JwtProperties;
import com.daengddang.daengdong_map.config.oauth.KakaoOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({ JwtProperties.class, KakaoOAuthProperties.class })
public class DaengdongMapApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaengdongMapApplication.class, args);
	}

}
