package com.example.student.service;

import com.example.student.dto.Message;
import com.example.student.service.implementation.StudentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    private StudentServiceImpl studentService;

    @Autowired
    public void setStudentService(StudentServiceImpl studentService) {
        this.studentService = studentService;
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void handleReceivedMessage(Message message) {
        log.info("Received message as specific class: {}", message.toString());
        studentService.notifyStudents(message);
    }
}
