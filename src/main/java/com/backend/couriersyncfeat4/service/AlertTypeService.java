package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.AlertTypeEntity;
import com.backend.couriersyncfeat4.repository.AlertTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertTypeService {

    private final AlertTypeRepository alertTypeRepository;

    @Autowired
    public AlertTypeService(AlertTypeRepository alertTypeRepository) {
        this.alertTypeRepository = alertTypeRepository;
    }

    public List<AlertTypeEntity> findAll(){
        return alertTypeRepository.findAll();
    }

    public AlertTypeEntity findById(int id){
        return alertTypeRepository.findById(id).orElseThrow(()->new RuntimeException("AlertTypeEntity not found"));
    }
}
