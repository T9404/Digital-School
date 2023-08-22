package com.example.notification.controller;

import com.example.notification.model.LocationRequest;
import com.example.notification.service.implementation.ProducerServiceImpl;
import com.example.notification.service.implementation.WeatherServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Tag(name = "producerController.title", description = "producerController.description")
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

    @Operation(summary = "api.notification.get-temperature")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.notification.response.success"),
            @ApiResponse(responseCode = "500", description = "api.notification.response.error"),
    })
    @GetMapping("/temperature")
    public ResponseEntity<String> getInfoWeatherWithParams(@RequestBody LocationRequest locationRequest,
                                                           @RequestParam("schoolName") String schoolName) {
        producerService.sendMessage(weatherService.getTemperature(locationRequest, schoolName));
        return ResponseEntity.ok("api.notification.response.success");
    }

    @Operation(summary = "api.notification.get-wind")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.notification.response.success"),
            @ApiResponse(responseCode = "500", description = "api.notification.response.error"),
    })
    @GetMapping("/wind")
    public ResponseEntity<String> getInfoWindWithParams(@RequestBody LocationRequest locationRequest,
                                                        @RequestParam("schoolName") String schoolName) {
        producerService.sendMessage(weatherService.getWindSpeed(locationRequest, schoolName));
        return ResponseEntity.ok("api.notification.response.success");
    }
}
