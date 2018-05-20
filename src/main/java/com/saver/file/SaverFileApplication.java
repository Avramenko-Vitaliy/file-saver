package com.saver.file;

import com.saver.file.props.AppProps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@SpringBootApplication
@EnableConfigurationProperties(AppProps.class)
public class SaverFileApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaverFileApplication.class, args);
	}

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port) {
        return new LettuceConnectionFactory(host, port);
    }
}
