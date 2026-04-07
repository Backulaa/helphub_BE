package com.helphub.backend;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelphubBackendApplication {

    public static void main(String[] args) {
        System.out.println("JVM timezone = " + TimeZone.getDefault().getID());
        SpringApplication.run(HelphubBackendApplication.class, args);
    }
}
