package com.example.notification.service;

import com.example.notification.model.LocationRequest;
import com.example.notification.model.MessageResponse;

public interface WeatherService {
    MessageResponse getTemperature(LocationRequest locationRequest, String schoolName);
    MessageResponse getWindSpeed(LocationRequest locationRequest, String schoolName);
}
