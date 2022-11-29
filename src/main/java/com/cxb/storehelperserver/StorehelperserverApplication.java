package com.cxb.storehelperserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * desc: 主程序
 * auth: cxb
 * date: 2022/11/29
 */
@SpringBootApplication
@EnableTransactionManagement
public class StorehelperserverApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(StorehelperserverApplication.class, args);
	}
}
