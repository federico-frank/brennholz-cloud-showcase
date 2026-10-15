package com.baumexpert.brennholzdemo.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class GlobalRabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        // Alle Nachrichten als JSON (Jackson 3) statt Java-Serialisierung
        return new JacksonJsonMessageConverter((JsonMapper) objectMapper);
    }
}
