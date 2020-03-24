package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RetryService {

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @Retryable(value = { RestClientException.class }, maxAttempts = 3, backoff = @Backoff(10L))
    public Map<String, Boolean> retryService() {
        System.out.println("Retryable");
        return restTemplate.getForObject("http://localhost:8080/retry-test", Map.class);
    }

    @Recover
    public Map<String, Boolean> recover(RestClientException e) {
        System.out.println("Recover");
        final Map<String, Boolean> map = new HashMap<>();
        map.put("success", false);
        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Boolean> retryServiceForRetryTemplate() {
        System.out.println("RetryTemplate");
        return restTemplate.getForObject("http://localhost:8080/retry-test", Map.class);
    }

    @SuppressWarnings("unchecked")
    @RetryAnnotation
    public Map<String, Boolean> retryServiceForRetryAdvice() {
        System.out.println("RetryAdvice");
        return restTemplate.getForObject("http://localhost:8080/retry-test", Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Boolean> retryServiceForRetryTemplateListener() {
        System.out.println("RetryTemplate Listener");
        return restTemplate.getForObject("http://localhost:8080/retry-test", Map.class);
    }

}
