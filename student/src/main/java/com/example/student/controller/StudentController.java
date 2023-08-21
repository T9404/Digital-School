package com.example.student.controller;

import com.example.student.entity.Notification;
import com.example.student.service.implementation.StudentServiceImpl;
import com.example.student.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentServiceImpl service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@RequestBody Student student) {
        service.saveStudent(student);
    }

    @GetMapping
    public ResponseEntity<List<Student>> findAllStudents() {
        return ResponseEntity.ok(service.findAllStudents());
    }

    @GetMapping("/school/{school-name}")
    public ResponseEntity<List<Student>> findAllStudents(@PathVariable("school-name") String schoolName) {
        return ResponseEntity.ok(service.findAllStudentsBySchool(schoolName));
    }

    @GetMapping("/notification/{student-name}")
    public ResponseEntity<List<Notification>> findStudentBySchoolName(@PathVariable("student-name") String studentName) {
        return ResponseEntity.ok(service.getNotificationByStudentId(studentName));
    }
}
