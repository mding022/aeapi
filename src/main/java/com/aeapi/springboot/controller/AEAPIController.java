package com.aeapi.springboot.controller;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

	@GetMapping("/createtbi")
	public String createtbi() {
		aeService = new AEServiceImpl();
		
		String[] files = {"1.png", "2.jpeg", "3.png", "4.png", "5.jpeg", "6.jpeg", "7.png", "8.png", "9.png", "10.png", "11.png"};


		String response = aeService.create(List.of(files));

		return response;
	}

	@PostMapping("/create")
	public ResponseEntity<Resource> create(@RequestParam("image1") MultipartFile image1, 
                                         @RequestParam("image2") MultipartFile image2) {
        
		aeService = new AEServiceImpl();

		try {

            aeService.saveFile("ae/images/1.jpg", image1);
            aeService.saveFile("ae/images/2.jpg", image2);
			String[] files = {"1.jpg", "2.jpg"};

			aeService.create(List.of(files));
			Path def = Paths.get("ae/output/Render_Comp 1_00002.mp4");
			String randomName = String.valueOf(ThreadLocalRandom.current().nextInt(1,1000000000));
			Path ran = Paths.get("ae/output/"+randomName+".mp4");
			Files.move(def, ran);

			Path png = Paths.get("ae/images/1.jpg");
			Path png2 = Paths.get("ae/images/2.jpg");
			Files.delete(png); Files.delete(png2);
			return getVideo(randomName+".mp4");

        } catch (IOException e) {
			System.out.println("Error taking files");
        }
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

	private static final String VIDEO_DIR = "ae/output"; // Change this to your actual folder path
    public ResponseEntity<Resource> getVideo(String filename) {
        try {
            Path videoPath = Paths.get(VIDEO_DIR).resolve(filename).normalize();
            if (Files.exists(videoPath) && Files.isReadable(videoPath)) {
                Resource resource = new UrlResource(videoPath.toUri());

                if (resource.exists() && resource.isReadable()) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
                    headers.add(HttpHeaders.CONTENT_TYPE, "video/mp4");

                    return ResponseEntity.ok()
                                         .headers(headers)
                                         .contentLength(resource.contentLength())
                                         .body(resource);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
