package com.alibaba.cloud.ai.plugin;

import com.alibaba.cloud.ai.plugin.GetWeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author zhang
 * @date 2024/11/21
 * @Description
 */

@TestPropertySource("classpath:application.yml")
public class GetWeatherTest {


    private GetWeatherService getWeatherService;
    @BeforeEach
    public void setUp() {
        getWeatherService = new GetWeatherService();
    }

    @Test
    public void testSearch() {
        GetWeatherService.Request request = new GetWeatherService.Request("beijing", null);
        GetWeatherService.Response response = getWeatherService.apply(request);

        System.out.println(response);

    }
}
