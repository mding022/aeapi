package com.aeapi.springboot.controller;

import com.aeapi.springboot.service.AEService;
import com.aeapi.springboot.service.impl.AEServiceImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@CrossOrigin(
    origins = {
        "http://localhost:3000",
        "https://millerding.com",
        "https://tunnel.millerding.com",
    }
)
public class AEAPIController {

    @Autowired
    public static AEService aeService;

    private final String storageDir = "ae/images";

    @GetMapping("/")
    public String index() {
        return "Successful";
    }

    @GetMapping("/aedata")
    public String aeTemplates() {
        try {
            BufferedReader br = new BufferedReader(
                new FileReader("ae/resources/templates.txt")
            );
            String s = br.readLine();
            br.close();
            return s;
        } catch (Exception e) {
            return "Error reading file";
        }
    }

    public String hololabsAPIdocs() {
        return "After Effects Get Templates#GET#api.hololabs.com/get_ae#api.hololabs.tech/get_ae?#Pulls a list of all currently available HoloLabs After Effects Templates#r#specify the return format#'json', 'txt', 'verbose-json'";
    }

    @GetMapping("/fsdata")
    public String fsTemplates() {
        try {
            BufferedReader br = new BufferedReader(
                new FileReader("ae/resources/fstemplates.txt")
            );
            String s = br.readLine();
            br.close();
            return s;
        } catch (Exception e) {
            return "Error reading file";
        }
    }

    @GetMapping("/fsvdata")
    public String fsvTemplates() {
        try {
            BufferedReader br = new BufferedReader(
                new FileReader("ae/resources/fsvtemplates.txt")
            );
            String s = br.readLine();
            br.close();
            return s;
        } catch (Exception e) {
            return "Error reading file";
        }
    }

    @GetMapping("/aiedata")
    public String aieData() {
        try {
            BufferedReader br = new BufferedReader(
                new FileReader("ae/resources/fstemplates.txt")
            );
            String s = br.readLine();
            br.close();
            return s;
        } catch (Exception e) {
            return "Error reading file";
        }
    }

    @GetMapping("/rbgdata")
    public String rbgData() {
        try {
            BufferedReader br = new BufferedReader(
                new FileReader("ae/resources/fstemplates.txt")
            );
            String s = br.readLine();
            br.close();
            return s;
        } catch (Exception e) {
            return "Error reading file";
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Resource> create(
        @RequestParam("request_id") String requestId,
        @RequestParam("template") String template
    ) {
        aeService = new AEServiceImpl();
        int image_count = 0;
        try {
            image_count = aeService.getTemplates().get(template);
        } catch (Exception e) {
            log.error("Unsuccessful request, template not found");
            return null;
        }
        log.info(
            "Received request to create mp4 using template {} with {} inputs.",
            template,
            image_count
        );
        List<String> files = new ArrayList<String>();
        try {
            File folder = new File("ae/images/" + requestId);
            File[] listOfFiles = folder.listFiles();
            for (File f : listOfFiles) {
                files.add(requestId + "/" + f.getName());
            }
            if (files.size() > 1) {
                log.info("Files output:");
                for (String s : files) {
                    System.out.println(s);
                }
            }
            aeService.create(files, template, image_count);
            Path def = Paths.get("ae/output/Render_Comp 1_00002.mp4");
            String randomName = String.valueOf(
                ThreadLocalRandom.current().nextInt(1, 1000000000)
            );
            Path ran = Paths.get("ae/output/" + randomName + ".mp4");
            Files.move(def, ran);

            //Delete files
            try {
                Path requestDir = Paths.get(storageDir, requestId);
                Files.walk(requestDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException ioe) {
                log.error("IOException from deleting");
            }

            return aeService.getVideo(randomName + ".mp4");
        } catch (IOException e) {
            log.error("Unexpected error occured during the process.");
            try {
                Path requestDir = Paths.get(storageDir, requestId);
                Files.walk(requestDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException ioe) {
                log.error("IOException from deleting");
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            null
        );
    }

    // public ResponseEntity<Resource> createGif(@RequestParam("template") String template) {

    // 	aeService = new AEServiceImpl();
    // 	int image_count = 0;
    // 	try{image_count = aeService.getTemplates().get(template);}
    // 	catch(Exception e) {
    // 		log.error("Unsuccessful request, template not found");
    // 		return null;
    // 	}
    // 	log.info("Received request to create gif using template {} with {} inputs.", template, image_count);
    // 	List<String> files = new ArrayList<String>();
    // 	try {

    // 		for(MultipartFile image : images) {
    // 			aeService.saveFile("ae/images/"+image.getOriginalFilename(), image);
    // 			files.add(image.getOriginalFilename());
    // 		}

    // 		aeService.create(files, template, image_count);
    // 		Path def = Paths.get("ae/output/Render_Comp 1_00002.mp4");
    // 		String randomName = String.valueOf(ThreadLocalRandom.current().nextInt(1,1000000000));
    // 		Path ran = Paths.get("ae/output/"+randomName+".mp4");
    // 		Files.move(def, ran);

    // 		for(String file : files) {
    // 			Files.delete(Paths.get("ae/images/"+file));
    // 		}
    // 		aeService.ffmpeg("ae/output/"+randomName+".mp4", "ae/output/"+randomName+".gif");
    // 		log.info("File: {}", randomName+".gif");
    // 		return aeService.getGif(randomName+".gif");
    //     } catch (IOException e) {
    // 		log.error("Unexpected error occured during the process.");
    // 		for(String file : files) {
    // 			try {
    // 				Files.delete(Paths.get("ae/images/"+file));
    // 			} catch (IOException e1) {
    // 				// TODO Auto-generated catch block
    // 				e1.printStackTrace();
    // 			}
    // 		}
    //     }
    // 	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    // }

    @GetMapping("templates")
    public Map<String, Integer> getTemplates() {
        return new AEServiceImpl().getTemplates();
    }

    @PostMapping("uploadfiles")
    public int uploadFiles(
        @RequestParam("images") List<MultipartFile> files,
        @RequestParam("request_id") String requestId
    ) {
        try {
            log.info("Received request with UUID: {}", requestId);
            Path requestDir = Paths.get(storageDir, requestId);
            Files.createDirectories(requestDir);

            for (MultipartFile file : files) {
                Path filePath = requestDir.resolve(file.getOriginalFilename());
                Files.copy(file.getInputStream(), filePath);
            }

            return 0;
        } catch (IOException ioe) {
            log.error("IOException from fss");
            return -1;
        }
    }
}
