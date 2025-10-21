package com.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ECommerceCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceCartServiceApplication.class, args);
	}

}
