package com.example.student.service.implementation;

import com.example.student.dto.Message;
import com.example.student.entity.Notification;
import com.example.student.entity.Student;
import com.example.student.mapper.MessageMapper;
import com.example.student.mapper.MessageMapperImpl;
import com.example.student.repository.NotificationRepository;
import com.example.student.repository.StudentRepository;
import com.example.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private StudentRepository studentRepository;
    private NotificationRepository notificationRepository;
    private final MessageMapper messageMapper;

    public StudentServiceImpl() {
        this.messageMapper = new MessageMapperImpl();
    }

    @Autowired
    public void setStudentRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void saveStudent(Student student) {
        studentRepository.save(student);
    }

    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> findAllStudentsBySchool(String schoolName) {
        return studentRepository.findAllBySchoolName(schoolName);
    }

    public List<Student> findStudentBySchoolName(String schoolName) {
        return studentRepository.findAllBySchoolName(schoolName);
    }

    public void notifyStudents(Message message) {

        List<Student> students = findStudentBySchoolName(message.schoolName());
        List<Notification> notifications = students.stream()
                .map(student -> messageMapper.toNotification(message, student))
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
    }

    @Override
    public List<Notification> getNotificationByStudentId(String studentName) {
        return notificationRepository.findAllByStudentName(studentName);
    }
}
