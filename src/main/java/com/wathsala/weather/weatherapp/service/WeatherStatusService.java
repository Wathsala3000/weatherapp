package com.wathsala.weather.weatherapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wathsala.weather.weatherapp.controller.WeatherStatusController;
import com.wathsala.weather.weatherapp.dto.Location;
import com.wathsala.weather.weatherapp.dto.MockData;
import com.wathsala.weather.weatherapp.dto.WeatherStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherStatusService {
     private final static String WEATHER_APP_URL = "http://dataservice.accuweather.com/currentconditions/v1/{locationkey}?apikey={apikey}&language=en-us";
     @Value("${accuweather.appkey}")
     private String appkey;
     @Autowired
     private RestTemplate restTemplate;

     public WeatherStatusService(RestTemplateBuilder restTemplateBuilder) {
          this.restTemplate = restTemplateBuilder.build();
     }
     public WeatherStatus getCurrentWeatherStatusByCity(String locationkey){
          ResponseEntity<String> response;
          try {
               URI url = new UriTemplate(WEATHER_APP_URL).expand(locationkey, appkey);
               response = restTemplate.getForEntity(url, String.class);
          }catch(Exception e){
               //set mock data
               //return getMapToWeatherStatus(new ResponseEntity<>(MockData.WetherStatus, HttpStatusCode.valueOf(202)));
               throw new RuntimeException("service error", e);
          }
          return getMapToWeatherStatus(response);
     }

     private WeatherStatus getMapToWeatherStatus(ResponseEntity<String> response) {
          ObjectMapper objectMapper = new ObjectMapper();
          WeatherStatus weatherStatus = new WeatherStatus();
          try{
               JsonNode root = objectMapper.readTree(response.getBody());
               if(root.isArray()) {
                    JsonNode firstNode = root.get(0);
                    weatherStatus.setWeatherText(firstNode.get("WeatherText").asText());
                    weatherStatus.setWeatherIcon(firstNode.get("WeatherIcon").asText());
                    Boolean dayTime = firstNode.get("IsDayTime").asBoolean();
                    if(Boolean.TRUE.equals(dayTime)){
                         weatherStatus.setDayTime("Day");
                    }else{
                         weatherStatus.setDayTime("Night");
                    }
                    weatherStatus.setDateAndtime(firstNode.get("LocalObservationDateTime").asText());
                    JsonNode temperatureMetricNode = firstNode.path("Temperature").path("Metric");
                    JsonNode temperatureImperialNode = firstNode.path("Temperature").path("Imperial");
                    StringBuilder sb = new StringBuilder();
                    sb.append(temperatureMetricNode.get("Value").asText())
                    .append(temperatureMetricNode.get("Unit").asText())
                    .append(" | ")
                    .append(temperatureImperialNode.get("Value").asText())
                    .append(temperatureImperialNode.get("Unit").asText());
                    weatherStatus.setTemperature(sb.toString());
               }
          }catch(JsonProcessingException e){
               throw new RuntimeException("JSON parser error", e);
          }
          return weatherStatus;
     }
}
