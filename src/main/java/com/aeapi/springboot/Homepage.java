package com.aeapi.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Homepage {

	@GetMapping("/")
	public String index() {
		return "Hello World!";
	}

}
