package de.hska.ld.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.rits.cloning.Cloner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.List;

@EnableWebMvc
@EnableAutoConfiguration

@Configuration
public class MvcConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        converter.setObjectMapper(objectMapper());
        converters.add(converter);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Hibernate4Module hibernate4Module = new Hibernate4Module();
        hibernate4Module.disable(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION);
        objectMapper.registerModule(hibernate4Module);
        return objectMapper;
    }

    @Bean
    public Cloner cloner() {
        return new Cloner();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
    }
}
