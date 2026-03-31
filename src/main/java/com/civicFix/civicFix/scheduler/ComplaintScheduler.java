package com.civicFix.civicFix.scheduler;

import com.civicFix.civicFix.entity.Complaint;
import com.civicFix.civicFix.entity.ComplaintStatus;
import com.civicFix.civicFix.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComplaintScheduler {

    private final ComplaintRepository complaintRepository;

    @Transactional
    @Scheduled(fixedRate = 30000)
    public void processComplaints() {

        List<Complaint> complaints =
                complaintRepository.findByStatus(ComplaintStatus.CREATED);

        if (complaints.isEmpty()) return;

        for (Complaint complaint : complaints) {
            if (complaint.getMunicipality() == null) continue;
            log.info("Complaint ID {} forwarded to Municipality ID {}",
                    complaint.getId(), complaint.getMunicipality().getId());
            complaint.setStatus(ComplaintStatus.SENT_TO_MUNICIPALITY);
        }

        complaintRepository.saveAll(complaints);
        log.info("Processed {} complaints", complaints.size());
    }
}