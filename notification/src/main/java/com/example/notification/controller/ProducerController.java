package com.example.notification.controller;

import com.example.notification.model.LocationRequest;
import com.example.notification.service.implementation.ProducerServiceImpl;
import com.example.notification.service.implementation.WeatherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class ProducerController {
    private ProducerServiceImpl producerService;
    private WeatherServiceImpl weatherService;

    @Autowired
    public void setProducerService(ProducerServiceImpl producerService) {
        this.producerService = producerService;
    }

    @Autowired
    public void setWeatherService(WeatherServiceImpl weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/temperature")
    public ResponseEntity<String> getInfoWeatherWithParams(@RequestBody LocationRequest locationRequest,
                                                           @RequestParam("schoolName") String schoolName) {
        producerService.sendMessage(weatherService.getTemperature(locationRequest, schoolName));
        return ResponseEntity.ok("Message sent");
    }

    @GetMapping("/wind")
    public ResponseEntity<String> getInfoWindWithParams(@RequestBody LocationRequest locationRequest,
                                                        @RequestParam("schoolName") String schoolName) {
        producerService.sendMessage(weatherService.getWindSpeed(locationRequest, schoolName));
        return ResponseEntity.ok("Message sent");
    }
}
