package com.example.student.mapper;

import com.example.student.dto.Message;
import com.example.student.entity.Notification;
import com.example.student.entity.Student;

public interface MessageMapper {
    Notification toNotification(Message message, Student student);
}
