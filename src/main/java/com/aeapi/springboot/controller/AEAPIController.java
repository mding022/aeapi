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
			String one = image1.getOriginalFilename();
			String two = image2.getOriginalFilename();
			System.out.println(one);
			System.out.println(two);
            aeService.saveFile("ae/images/"+one, image1);
            aeService.saveFile("ae/images/"+two, image2);
			String[] files = {one, two};

			aeService.create(List.of(files));
			Path def = Paths.get("ae/output/Render_Comp 1_00002.mp4");
			String randomName = String.valueOf(ThreadLocalRandom.current().nextInt(1,1000000000));
			Path ran = Paths.get("ae/output/"+randomName+".mp4");
			Files.move(def, ran);

			Path png = Paths.get("ae/images/"+one);
			Path png2 = Paths.get("ae/images/"+two);
			Files.delete(png); Files.delete(png2);
			aeService.ffmpeg("ae/output/"+randomName+".mp4", "ae/output/"+randomName+".gif");
			System.out.println("File: " + randomName+".gif");
			return aeService.getGif(randomName+".gif");

        } catch (IOException e) {
			System.out.println("Error taking files");
        }
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

	
}
