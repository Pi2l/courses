package org.m.courses.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JsonFormatterConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, ZonedDateTime>() {
            public ZonedDateTime convert(String source) {
                return ZonedDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
            }
        });
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .timeZone( TimeZone.getDefault() );
    }
}
