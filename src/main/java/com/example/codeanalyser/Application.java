package com.example.codeanalyser;
import org.springframework.beans.factory.annotation.Autowired;  // Adjust the package name exactly as per your TestLogger
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class Application {

    @Autowired
    private TestLogger loggerTest;  // Inject LoggerTest bean

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void testLogging() {
        loggerTest.test();  // Call the instance method, not static
    }
}
