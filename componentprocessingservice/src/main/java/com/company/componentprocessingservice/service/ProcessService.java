package com.company.componentprocessingservice.service;

import com.company.componentprocessingservice.entity.ProcessResponse;

public interface ProcessService {
    ProcessResponse processDetail(Long requestId);
}