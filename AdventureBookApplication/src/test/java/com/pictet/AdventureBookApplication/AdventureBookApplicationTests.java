package com.pictet.AdventureBookApplication;

import static org.assertj.core.api.Assertions.assertThat;

import com.pictet.AdventureBookApplication.service.GameEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdventureBookApplicationTests {

	@Autowired
	private GameEngineService gameEngineService;

	@Test
	void contextLoads() {
		assertThat(gameEngineService).isNotNull();
	}

}
