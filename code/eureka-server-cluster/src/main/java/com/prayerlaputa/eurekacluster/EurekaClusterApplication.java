package com.prayerlaputa.eurekacluster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaClusterApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClusterApplication.class, args);
    }

}
