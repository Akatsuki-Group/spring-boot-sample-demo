package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan("org.example.filter")
public class FilterApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilterApplication.class, args);
    }
}
