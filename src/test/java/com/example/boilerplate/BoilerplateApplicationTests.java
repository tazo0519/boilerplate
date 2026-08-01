package com.example.boilerplate;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class BoilerplateApplicationTests {

    @Test
    void contextLoads() {
    }

}
