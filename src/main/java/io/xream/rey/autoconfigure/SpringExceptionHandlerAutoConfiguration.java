package io.xream.rey.autoconfigure;

import io.xream.rey.spring.exceptionhandler.Spring400ExceptionHandler;
import org.springframework.context.annotation.Bean;

/**
 * @author Sim
 */
public class SpringExceptionHandlerAutoConfiguration {

    @Bean
    public Spring400ExceptionHandler spring400ExceptionHandler(){
        return new Spring400ExceptionHandler();
    }
}
