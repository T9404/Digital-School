package com.example.student.repository;

import com.example.student.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.student.firstname = ?1")
    List<Notification> findAllByStudentName(String studentName);
}
