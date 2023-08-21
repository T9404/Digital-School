package com.example.notification.service;

import com.example.notification.model.MessageResponse;

public interface ProducerService {
    void sendMessage(MessageResponse messageResponse);
}
