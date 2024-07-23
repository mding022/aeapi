package com.aeapi.springboot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aeapi.springboot.service.AEService;
import com.aeapi.springboot.service.impl.AEServiceImpl;

@RestController
public class AEAPIController {

	@Autowired
	public static AEService aeService;


	@GetMapping("/")
	public String index() {
		aeService = new AEServiceImpl();
		
		String[] files = {"1.png", "2.jpeg", "3.png", "4.png", "5.jpeg", "6.jpeg", "7.png", "8.png", "9.png", "10.png", "11.png"};
		
		aeService.create(List.of(files));

		return "running scripts.";
	}

}
