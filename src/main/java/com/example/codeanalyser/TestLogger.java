package com.example.codeanalyser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestLogger {

    private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);

    public void test() {
        logger.info("Logging works!");
    }
}
