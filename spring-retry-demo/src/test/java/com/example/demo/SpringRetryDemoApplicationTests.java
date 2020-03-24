package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;

@SpringBootTest
class SpringRetryDemoApplicationTests {

    @Autowired
    private RetryService service;

    @Autowired
    @Qualifier("retryTemplate")
    private RetryTemplate retryTemplate;

    @Autowired
    @Qualifier("listenerRetryTemplate")
    private RetryTemplate listenerRetryTemplate;

	@Test
    public void retryAnnotations() {
        assertThat(this.service).isNotNull();

        final Map<String, Boolean> actual = this.service.retryService();
        assertThat(actual).isNotEmpty().allSatisfy((key, value) -> {
            assertThat(key).isIn("success");
            assertThat(value).isIn(false);
        });
        System.out.println(actual);
	}

    @Test
    public void retryTemplate() {
        final Map<String, Boolean> actual = this.retryTemplate.execute(
                (RetryCallback<Map<String, Boolean>, RestClientException>) context -> this.service.retryServiceForRetryTemplate(),
                (RecoveryCallback<Map<String, Boolean>>) context -> {
                    final Map<String, Boolean> map = new HashMap<>();
                    map.put("success", false);
                    return map;
                });
        assertThat(actual).isNotEmpty().allSatisfy((key, value) -> {
            assertThat(key).isIn("success");
            assertThat(value).isIn(false);
        });
        System.out.println(actual);
    }

    @Test
    public void retryAdvice() {
        final Map<String, Boolean> actual = this.service.retryServiceForRetryAdvice();
        assertThat(actual).isNotEmpty().allSatisfy((key, value) -> {
            assertThat(key).isIn("success");
            assertThat(value).isIn(false);
        });
        System.out.println(actual);
    }

    @Test
    public void retryListener() {
        final Map<String, Boolean> actual = this.listenerRetryTemplate
                .execute(
                        (RetryCallback<Map<String, Boolean>, RestClientException>) context -> this.service
                                .retryServiceForRetryTemplateListener(),
                        (RecoveryCallback<Map<String, Boolean>>) context -> {
                            final Map<String, Boolean> map = new HashMap<>();
                            map.put("success", false);
                            return map;
                        });
        assertThat(actual).isNotEmpty().allSatisfy((key, value) -> {
            assertThat(key).isIn("success");
            assertThat(value).isIn(false);
        });
        System.out.println(actual);
    }

}
