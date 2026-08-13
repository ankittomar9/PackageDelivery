package com.company.componentprocessingservice.repository;

import com.company.componentprocessingservice.entity.ProcessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRequestRepository extends JpaRepository<ProcessRequest, Long> {
}
