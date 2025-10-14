package com.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ECommerceProductCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceProductCatalogApplication.class, args);
	}

}
