package com.company.componentprocessingservice.repository;

import com.company.componentprocessingservice.entity.ProcessResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessResponseRepository extends JpaRepository<ProcessResponse, Long> {
}
