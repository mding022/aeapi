package com.aeapi.springboot.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.aeapi.springboot.models.Task;

public interface AEService {
    
    public String create(List<String> images);

    public void saveFile(String uploadDir, MultipartFile file) throws IOException;

    public void ffmpeg(String input, String output);

    public ResponseEntity<Resource> getGif(String filename);

    public ResponseEntity<Resource> getVideo(String filename);

    public Map<String, Integer> getTemplates();
}
