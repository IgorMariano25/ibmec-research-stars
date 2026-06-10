package br.com.ibmec.researchstars.config;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateTimeConfig {

    public static final ZoneId SAO_PAULO_ZONE = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock applicationClock() {
        return Clock.system(SAO_PAULO_ZONE);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer saoPauloJacksonTimeZone() {
        return builder -> builder
                .timeZone(TimeZone.getTimeZone(SAO_PAULO_ZONE))
                .dateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone(SAO_PAULO_ZONE)));
    }
}
