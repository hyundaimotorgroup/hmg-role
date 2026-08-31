package com.hmg.role;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    @Autowired protected ScenarioContext scenarioContext;

    @Autowired protected ObjectMapper mapper;

    @LocalServerPort protected Integer localServerPort;

    protected RequestSpecification request() {
        return RestAssured.given().baseUri("http://localhost:" + localServerPort);
    }
}
