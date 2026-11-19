package com.teamnote;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "TEAMNOTE API", version = "1.0"))
@SpringBootApplication
public class TeamnoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamnoteApplication.class, args);
    }

}
