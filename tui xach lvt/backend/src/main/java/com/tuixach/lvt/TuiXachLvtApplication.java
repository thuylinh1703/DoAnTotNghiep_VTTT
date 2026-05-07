package com.tuixach.lvt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class})
@EnableScheduling
public class TuiXachLvtApplication {
    public static void main(String[] args) {
        SpringApplication.run(TuiXachLvtApplication.class, args);
    }
}
