package com.example.school.client;

import com.example.school.dto.Student;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "student-service", url = "${application.config.student-url}")
public interface StudentClient {

    @GetMapping("/school/{school-name}")
    List<Student> findAllStudentsBySchool(
            @PathVariable("school-name") String schoolName,
            @RequestHeader("Authorization") String authorizationHeader);
}
