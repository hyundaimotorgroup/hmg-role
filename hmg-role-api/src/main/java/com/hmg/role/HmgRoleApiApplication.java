package com.hmg.role;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@OpenAPIDefinition(servers = {@Server(url = "/")})
@SpringBootApplication
@EnableAsync
public class HmgRoleApiApplication {

    public static void main(String[] args) {

        SpringApplication.run(HmgRoleApiApplication.class, args);
    }
}
