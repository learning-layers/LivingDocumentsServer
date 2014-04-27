package de.hska.livingdocuments.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"de.hska.livingdocuments.*.config", "de.hska.livingdocuments.*.persistence",
        "de.hska.livingdocuments.*.controller"})
@EnableAutoConfiguration
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
