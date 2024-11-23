/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.plugin;

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
        WeatherProperties properties = new WeatherProperties();
        properties.setApiKey("API_KEY");
        getWeatherService = new GetWeatherService(properties);
    }

    @Test
    public void testSearch() {
        GetWeatherService.Request request = new GetWeatherService.Request("beijing", 0);
        GetWeatherService.Response response = getWeatherService.apply(request);
        assertNotNull(response);

    }
}
