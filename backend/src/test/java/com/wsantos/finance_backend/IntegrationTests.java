package com.wsantos.finance_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(properties = "DB_SCHEMA=public")
@AutoConfigureMockMvc 
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTests {
    @Autowired 
    protected MockMvcTester mvc;
    
}
