package com.james.curbee.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.james.curbee.util.LocaleMessageSource;

@Configuration
public class ErrorMessageConfiguration {

    @Bean
    public LocaleMessageSource localeMessageSource() {
        LocaleMessageSource messageSource = new LocaleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("messages");
        return messageSource;
    }

}
