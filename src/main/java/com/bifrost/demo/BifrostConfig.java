package com.bifrost.demo;

import com.bifrost.demo.dto.model.OTP;
import com.bifrost.demo.dto.model.ResumeRoastCache;
import com.bifrost.demo.middleware.AdminEndpointInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
public class BifrostConfig implements WebMvcConfigurer {
    private final AdminEndpointInterceptor adminEndpointInterceptor;

    public BifrostConfig(AdminEndpointInterceptor adminEndpointInterceptor) {
        this.adminEndpointInterceptor = adminEndpointInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminEndpointInterceptor)
                .addPathPatterns("/v1/admin/**");
    }

    @Bean
    @ConditionalOnProperty(value = "admin.redis.enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, OTP> redisOTPTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, OTP> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(OTP.class));
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    @ConditionalOnProperty(value = "admin.redis.enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, ResumeRoastCache> redisResumeRoastCachingTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ResumeRoastCache> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ResumeRoastCache.class));
        template.afterPropertiesSet();

        return template;
    }

    @Bean(name = "emailTaskExecutor")
    public TaskExecutor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }

    @Bean(name = "resumeRoasterTaskExecutor")
    public TaskExecutor resumeRoasterTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ResumeRoaster-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }
}
