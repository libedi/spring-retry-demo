package com.example.demo;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableRetry
@RestController
@Aspect
public class SpringRetryDemoApplication {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(60L))
                .setReadTimeout(Duration.ofSeconds(60L))
                .build();
    }

    // RetryTemplate configuration
    @Bean
    public RetryTemplate retryTemplate() {
        final RetryTemplate retryTemplate = new RetryTemplate();

        final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(2);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    // AOP configuration
    @Bean
    public Advisor retryAdvisor() {
        final AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(com.example.demo.RetryAnnotation)");
        return new DefaultPointcutAdvisor(pointcut, taskRetryAdvice());
    }

    @Bean
    public Advice taskRetryAdvice() {
//        final RetryOperationsInterceptor advice = new RetryOperationsInterceptor();
//        advice.setRetryOperations(taskRetryTemplate());
//        advice.setRecoverer((args, e) -> {
//            final Map<String, Boolean> map = new HashMap<>();
//            map.put("success", false);
//            return map;
//        });
//        return advice;
        return RetryInterceptorBuilder.stateless()
                .retryPolicy(taskRetryPolicy())
//                .maxAttempts(5)
                .backOffOptions(300, 2.0, 30000)
                .recoverer((args, e) -> {
                    final Map<String, Boolean> map = new HashMap<>();
                    map.put("success", false);
                    return map;
                })
                .build();
    }

//    @Bean
//    public RetryOperations taskRetryTemplate() {
//        final RetryTemplate template = new RetryTemplate();
//        template.setRetryPolicy(taskRetryPolicy());
//        template.setBackOffPolicy(exponentialBackOffPolicy());
//        return template;
//    }

    @Bean
    public RetryPolicy taskRetryPolicy() {
        final int maxAttempts = 5;
        final Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(RestClientException.class, true);
        return new SimpleRetryPolicy(maxAttempts, retryableExceptions);
    }

//    @Bean
//    public BackOffPolicy exponentialBackOffPolicy() {
//        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
//        backOffPolicy.setInitialInterval(300);
//        backOffPolicy.setMaxInterval(30000);
//        backOffPolicy.setMultiplier(2.0);
//        return backOffPolicy;
//    }

    // Listner configuration
    @Bean
    public RetryTemplate listenerRetryTemplate() {
        final RetryTemplate retryTemplate = new RetryTemplate();

        final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(2);
        retryTemplate.setRetryPolicy(retryPolicy);

        retryTemplate.registerListener(new RetryListener() {

            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                System.out.println("open");
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
                    Throwable throwable) {
                System.out.println("close");
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                    Throwable throwable) {
                System.out.println("onError");
            }
        });

        return retryTemplate;
    }

    // ----------
    public static void main(String[] args) {
		SpringApplication.run(SpringRetryDemoApplication.class, args);
	}
	
    @GetMapping("/retry-test")
    public Map<String, Boolean> test() {
        final Map<String, Boolean> map = new HashMap<>();
        if (map.isEmpty()) {
            throw new RuntimeException("call failed.");
        }
        return map;
    }
    
}
