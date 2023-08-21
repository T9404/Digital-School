package com.example.student.mapper;

import com.example.student.dto.Message;
import com.example.student.entity.Notification;
import com.example.student.entity.Student;

public class MessageMapperImpl implements MessageMapper {

    @Override
    public Notification toNotification(Message message, Student student) {
        return Notification.builder()
                .message(message.message())
                .date(message.myDate())
                .ownerNotification(message.owner())
                .student(student)
                .build();
    }
}
