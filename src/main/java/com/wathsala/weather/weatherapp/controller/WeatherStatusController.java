package com.wathsala.weather.weatherapp.controller;

import com.wathsala.weather.weatherapp.dto.Location;
import com.wathsala.weather.weatherapp.dto.WeatherStatus;
import com.wathsala.weather.weatherapp.service.LocationService;
import com.wathsala.weather.weatherapp.service.WeatherStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WeatherStatusController {

    @Autowired
    LocationService locationService;
    @Autowired
    WeatherStatusService weatherStatusService;
    @RequestMapping(path="/", method= RequestMethod.GET)
    public String weatherHome(Model model){

        List<Location> locationList =  locationService.getLocationList(50);
        if(locationList!=null && !locationList.isEmpty()){
            model.addAttribute("Location", locationList);
        }
        return "WeatherHome";
    }

    @RequestMapping(path="/getCurrentWeatherStatus", method=RequestMethod.GET)
    public String getCurrentWeatherStatus(@RequestParam(name="locationkey",required=true) String locationKey,
                                          @RequestParam(name="location",required=true) String location,
                                          Model model){
        WeatherStatus weatherStatus = weatherStatusService.getCurrentWeatherStatusByCity(locationKey);
        weatherStatus.setLocation(location);
        model.addAttribute("WeatherStatus",weatherStatus);
        return "WeatherStatus";
    }
}
