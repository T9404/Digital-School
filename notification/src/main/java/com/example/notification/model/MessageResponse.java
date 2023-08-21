package com.example.notification.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Builder
public record MessageResponse(@NonNull String message,
                              @NonNull String owner,
                              @NonNull String schoolName,
                              @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                              Date myDate) {
}
