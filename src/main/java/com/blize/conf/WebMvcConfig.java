package com.blize.conf;

import com.blize.service.files.FileService;
import de.smartsquare.socketio.emitter.Emitter;
import de.smartsquare.socketio.emitter.SpringDataPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.CacheControl;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableRedisHttpSession
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private HeaderInterceptor headerInterceptor;
    @Autowired
    private FileService fileService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(this.headerInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/dist/**", "/images/**", "/files/**", "/css/**", "/js/**", "/favicon.ico");

        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/images/**")
                .addResourceLocations("file:" + this.fileService.getResourcesPath() + "/static/files/images/")
                .setCacheControl(CacheControl.noCache())
                .setCachePeriod(0);
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        //for session
        return new JdkSerializationRedisSerializer();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);

        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public Emitter emitter(StringRedisTemplate stringRedisTemplate) {
        return new Emitter(new SpringDataPublisher(stringRedisTemplate));
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {
            @Override
            public OffsetDateTime convert(Date date) {
                return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
        }

        class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {
            @Override
            public Date convert(OffsetDateTime offsetDateTime) {
                return Date.from(offsetDateTime.toInstant());
            }
        }

        return new MongoCustomConversions(List.of(
                new OffsetDateTimeReadConverter(),
                new OffsetDateTimeWriteConverter()));
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        // change by Accept-Language
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        // NOt URL use Header
        return new LocaleChangeInterceptor();
    }
}