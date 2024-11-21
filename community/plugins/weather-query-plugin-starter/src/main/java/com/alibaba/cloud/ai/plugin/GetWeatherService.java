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

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

public class GetWeatherService
        implements Function<GetWeatherService.Request, GetWeatherService.Response> {

    private static final Logger logger = LoggerFactory.getLogger(GetWeatherService.class);

    private static final String WEATHER_API_URL = "https://api.weatherapi.com/v1/current.json";

    private final WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.alibaba.plugin.weather.api-key}")
    private String apiKey;

    public GetWeatherService() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT,
                        "application/json, text/plain, */*")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,ja;q=0.8")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }
    @Override
    public Response apply(Request request) {
        if (request == null || !StringUtils.hasText(request.city())) {
            logger.error("Invalid request: city is required.");
            return null;
        }

        String url = UriComponentsBuilder.fromHttpUrl(WEATHER_API_URL)
                .queryParam("key", apiKey)
                .queryParam("q", request.city())
                .queryParam("dt", request.date() == null ? "" : request.date())
                .toUriString();

        try {
            Mono<String> responseMono = webClient.get().uri(url).retrieve().bodyToMono(String.class);
            String jsonResponse = responseMono.block();
            assert jsonResponse != null;

            Response response = fromJson(
                    objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {})
            );
            logger.info("Weather data fetched successfully for city: {}", response.city());
            return response;
        } catch (Exception e) {
            logger.error("Failed to fetch weather data: {}", e.getMessage());
            return null;
        }
    }


    public static Response fromJson(Map<String, Object> json) {
        Map<String, Object> location = (Map<String, Object>) json.get("location");
        Map<String, Object> current = (Map<String, Object>) json.get("current");

        String city = (String) location.get("name");
        String temperature = String.valueOf(current.get("temp_c"));
        Map<String, Object> condition = (Map<String, Object>) current.get("condition");
        String conditionText = (String) condition.get("text");
        String humidity = current.containsKey("humidity") ? String.valueOf(current.get("humidity")) : null;
        String windSpeed = current.containsKey("wind_kph") ? String.valueOf(current.get("wind_kph")) : null;

        return new Response(city, temperature, conditionText, humidity, windSpeed);
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Weather Service API request")
    public record Request(
            @JsonProperty(required = true, value = "city") @JsonPropertyDescription("THE CITY OF INQUIRY") String city,
            @JsonProperty(required = true, value = "data") @JsonPropertyDescription("THE TIME OF THE QUERY") String date

    ){}


    @JsonClassDescription("Weather Service API response")
    public record Response(
            @JsonProperty(required = true, value = "city") @JsonPropertyDescription("The name of the city for the weather report") String city,

            @JsonProperty(required = true, value = "temperature") @JsonPropertyDescription("Current temperature in the city, measured in Celsius") String temperature,

            @JsonProperty(required = true, value = "condition") @JsonPropertyDescription("Current weather condition description, e.g., 'Cloudy', 'Sunny'") String condition,

            @JsonProperty(required = false, value = "humidity") @JsonPropertyDescription("Current humidity percentage in the city") String humidity,

            @JsonProperty(required = false, value = "windSpeed") @JsonPropertyDescription("Wind speed in the city, measured in km/h") String windSpeed
    ) {}
}
