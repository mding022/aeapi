package com.aeapi.springboot.controller;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aeapi.springboot.service.AEService;
import com.aeapi.springboot.service.impl.AEServiceImpl;

import io.micrometer.core.ipc.http.HttpSender.Response;

@RestController
public class AEAPIController {

	@Autowired
	public static AEService aeService;


	@GetMapping("/")
	public String index() {
		return "Success";
	}

	@PostMapping("/create")
	public ResponseEntity<Resource> create(@RequestParam("images") List<MultipartFile> images, @RequestParam("template") String template) {
        
		aeService = new AEServiceImpl();

		try{int image_count = aeService.getTemplates().get(template);System.out.println("count images: " + image_count);}
		catch(Exception e) {
			System.out.println("Template not found!");
			return null;
		}

		try {
			List<String> files = new ArrayList<String>();

			for(MultipartFile image : images) {
				aeService.saveFile("ae/images/"+image.getOriginalFilename(), image);
				files.add(image.getOriginalFilename());
			}
			

			aeService.create(files);
			Path def = Paths.get("ae/output/Render_Comp 1_00002.mp4");
			String randomName = String.valueOf(ThreadLocalRandom.current().nextInt(1,1000000000));
			Path ran = Paths.get("ae/output/"+randomName+".mp4");
			Files.move(def, ran);

			for(String file : files) {
				Files.delete(Paths.get("ae/images/"+file));
			}
			aeService.ffmpeg("ae/output/"+randomName+".mp4", "ae/output/"+randomName+".gif");
			System.out.println("File: " + randomName+".gif");
			return aeService.getGif(randomName+".gif");

        } catch (IOException e) {
			System.out.println("Error taking files");
        }
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

	@GetMapping("templates")
	public Map<String, Integer> getTemplates() {
		return new AEServiceImpl().getTemplates();
	}

	
}
