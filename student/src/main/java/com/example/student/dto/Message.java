package com.example.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.NonNull;

import java.util.Date;

@Builder
public record Message(@NonNull String message,
                      @NonNull String owner,
                      @NonNull String schoolName,
                      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                      Date myDate) {
}
