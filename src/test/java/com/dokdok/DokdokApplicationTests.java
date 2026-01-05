package com.dokdok;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DokdokApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test
    void checkEnvironmentVariable() {
        String password = System.getenv("DB_PASSWORD");
        System.out.println("DB_PASSWORD from env: " + password);

        String fromProperty = System.getProperty("DB_PASSWORD");
        System.out.println("DB_PASSWORD from property: " + fromProperty);
    }
}
