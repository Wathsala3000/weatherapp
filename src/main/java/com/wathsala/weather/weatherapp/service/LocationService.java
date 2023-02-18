package com.wathsala.weather.weatherapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wathsala.weather.weatherapp.dto.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.*;

@Service
public class LocationService {
    private final static String locationServiceUrl = "http://dataservice.accuweather.com/locations/v1/topcities/{count}?apikey={apikey}&language=en-us";
    @Value("${accuweather.appkey}")
    private String appkey;
    @Autowired
    RestTemplate restTemplate;

    public LocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Cacheable(value = "locationList")
    public List<Location> getLocationList(int listSize){
        ResponseEntity<String> response;
        try {
            URI url = new UriTemplate(locationServiceUrl).expand(listSize, appkey);
            response = restTemplate.getForEntity(url, String.class);
        }catch(Exception e){
            //set mock data
           //return getMapToLocationList(new ResponseEntity<>(MockData.locationList,HttpStatusCode.valueOf(202)));
           throw new RuntimeException("service error", e);
        }
        return getMapToLocationList(response);
    }
    private List<Location> getMapToLocationList(ResponseEntity<String> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Location> locationList = new ArrayList<>();
        try{
            JsonNode root = objectMapper.readTree(response.getBody());
          if(root.isArray()){
              for (JsonNode jsonNode : root) {
                  String key = jsonNode.get("Key").asText();
                  String englishName = jsonNode.get("LocalizedName").asText();
                  String type = jsonNode.get("Type").asText();
                  locationList.add(new Location(englishName,key,type));
              }
          }
        }catch(JsonProcessingException e){
            throw new RuntimeException("JSON parser error", e);
        }
        return locationList;
    }
}
