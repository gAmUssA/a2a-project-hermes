package com.a2a.kafka.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.a2a.kafka")
public class A2AWebApplication {

  public static void main(String[] args) {
    SpringApplication.run(A2AWebApplication.class, args);
  }
}