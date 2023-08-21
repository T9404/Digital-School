package com.example.notification.service.implementation;

import com.example.notification.model.LocationRequest;
import com.example.notification.model.MessageResponse;
import com.example.notification.exception.RequestFailedException;
import com.example.notification.exception.WeatherInfoException;
import com.example.notification.service.WeatherService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Value("${app.url.weather}")
    private String apiUrl;

    @Value("${app.warning.temperature.hot}")
    private int hotWeatherWarning;

    @Value("${app.warning.temperature.cold}")
    private int coldWeatherWarning;

    @Value("${app.warning.wind.speed}")
    private int windSpeedWarning;

    @Override
    public MessageResponse getTemperature(LocationRequest locationRequest, String schoolName) {
        double temperature = getCurrentWeatherInfo(locationRequest).get("temperature").getAsDouble();
        return MessageResponse.builder()
                .message(getTemperatureWarning(temperature))
                .owner("open-meteo.com")
                .schoolName(schoolName)
                .myDate(new Date())
                .build();
    }

    private String getTemperatureWarning(double temperature) {
        if (temperature < coldWeatherWarning || temperature > hotWeatherWarning) {
            return "WARNING. Children should not go outside. Temperature: " + temperature + " degrees";
        }
        return "It's safe to go outside. Temperature: " + temperature + " degrees";
    }

    @Override
    public MessageResponse getWindSpeed(LocationRequest locationRequest, String schoolName) {
        double windSpeed = getCurrentWeatherInfo(locationRequest).get("windspeed").getAsDouble();
        return MessageResponse.builder()
                .message(getWindWarning(windSpeed))
                .owner("open-meteo.com")
                .schoolName(schoolName)
                .myDate(new Date())
                .build();
    }

    private String getWindWarning(double windSpeed) {
        if (windSpeed > windSpeedWarning) {
            return "WARNING. Children should not go outside. Wind speed: " + windSpeed + " m/s";
        }
        return "It's safe to go outside. Wind speed: " + windSpeed + " m/s";
    }

    private JsonObject getCurrentWeatherInfo(LocationRequest locationRequest) {
        try {
            String address = apiUrl + "?latitude=" + locationRequest.latitude()
                    + "&longitude=" + locationRequest.longitude() + "&current_weather=true";
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            return handleConnection(connection);
        } catch (Exception e) {
            throw new WeatherInfoException(e.getMessage());
        }
    }

    private JsonObject handleConnection(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = readGettingResponse(in);
            return getCurrentWeatherField(response);
        } else {
            throw new RequestFailedException(responseCode);
        }
    }

    private JsonObject getCurrentWeatherField(StringBuilder response) {
        JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
        return jsonObject.getAsJsonObject("current_weather");
    }

    private StringBuilder readGettingResponse(BufferedReader in) {
        return new StringBuilder(in.lines().collect(Collectors.joining()));
    }
}
