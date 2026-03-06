package com.civicFix.civicFix.scheduler;

import com.civicFix.civicFix.entity.Complaint;
import com.civicFix.civicFix.entity.ComplaintStatus;
import com.civicFix.civicFix.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@RequiredArgsConstructor
public class ComplaintScheduler {

    private final ComplaintRepository complaintRepository;

    @Scheduled(fixedRate = 30)
    public void processComplaints() {

        List<Complaint> complaints =
                complaintRepository.findByStatus(ComplaintStatus.CREATED);

        for (Complaint complaint : complaints) {

            if (complaint.getMunicipality() == null) {
                continue;
            }

            System.out.println(
                    "Complaint ID " + complaint.getId() +
                            " forwarded to Municipality ID " +
                            complaint.getMunicipality().getId()
            );

            complaint.setStatus(ComplaintStatus.SENT_TO_MUNICIPALITY);
        }

        complaintRepository.saveAll(complaints);
    }
}