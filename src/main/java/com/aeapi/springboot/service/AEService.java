package com.aeapi.springboot.service;

import java.util.List;

import com.aeapi.springboot.models.Task;

public interface AEService {
    
    public int create(List<String> tasks);

}
