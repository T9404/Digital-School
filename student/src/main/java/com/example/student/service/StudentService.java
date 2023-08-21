package com.example.student.service;

import com.example.student.dto.Message;
import com.example.student.entity.Notification;
import com.example.student.entity.Student;

import java.util.List;

public interface StudentService {
    void notifyStudents(Message message);
    List<Notification> getNotificationByStudentId(String studentName);
    List<Student> findAllStudentsBySchool(String schoolName);
    List<Student> findAllStudents();
    void saveStudent(Student student);
}
