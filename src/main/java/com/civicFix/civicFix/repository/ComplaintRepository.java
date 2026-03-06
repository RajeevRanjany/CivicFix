package com.civicFix.civicFix.repository;

import com.civicFix.civicFix.entity.Complaint;
import com.civicFix.civicFix.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime time);
    List<Complaint> findByStatus(ComplaintStatus status);
}