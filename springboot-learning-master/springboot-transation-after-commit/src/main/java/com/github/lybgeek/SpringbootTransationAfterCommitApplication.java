package com.github.lybgeek;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = "com.github.lybgeek.user.**.dao")
public class SpringbootTransationAfterCommitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootTransationAfterCommitApplication.class, args);
	}

}
