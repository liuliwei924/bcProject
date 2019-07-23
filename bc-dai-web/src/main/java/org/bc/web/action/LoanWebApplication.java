package org.bc.web.action;

import org.bc.web.action.timer.TranferDataTimer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import lombok.extern.slf4j.Slf4j;

@EnableRedisHttpSession
@SpringBootApplication
@ComponentScan(basePackages = { "org.bc.*" ,"org.llw.*"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Slf4j
public class LoanWebApplication {

	public static void main(String[] args) {
		log.info("=============loan client web is run {}===============","start");
		SpringApplication.run(LoanWebApplication.class, args);
		log.info("=============loan client web is run {}=============","success");
		
		//初始化转单定时器
		Thread tf = new Thread(new TranferDataTimer());
		tf.setName("tranferData-thread");
		tf.start();
	}
}
