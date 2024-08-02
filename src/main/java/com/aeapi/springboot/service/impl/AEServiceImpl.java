package com.aeapi.springboot.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aeapi.springboot.models.Task;
import com.aeapi.springboot.service.AEService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AEServiceImpl implements AEService{
    
    public String create(List<String> images, String template, int img_count) {
        //clean directory
        emptyFolder("ae/output");
        List<Task> tasks = new ArrayList<Task>();
        Task testingTask = new Task();
        testingTask.setImages(images);
        tasks.add(testingTask);

        int er = env(template);
        if(er != 0) {
            log.error("Could not access templater-json file");
            return "error";
        }
        Task t = new Task();
        t.setImages(images);
        List<Task> tl = new ArrayList<Task>();
        tl.add(t);

        er = writeToFile("ae/temp.tsv", img_count, tl);

        runner(new String[] {"ae/./templater.sh", "-v", "2024", "-m"});

        return "done";
    }

    private static int env(String template) {
        List<String> lines = new ArrayList<>();
        File json = new File("templater-options.json");
        if(!json.exists()) {
            log.warn("templater-options json file does not exist in ~/ae");
            return -1;
        }
        try (BufferedReader br = new BufferedReader(new FileReader("templater-options.json"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f = new File("ae/"+template);
        if(f.exists()) {
            log.info("Found matching project file: {}", f.getAbsolutePath());
            lines.set(3, ", \"aep\"                   : \""+f.getAbsolutePath()+"\"");
            f = new File("ae/logs");
            lines.set(1, "  \"log_location\"          : \""+f.getAbsolutePath()+"\"");
            f = new File("ae/temp.tsv");
            lines.set(2, ", \"data_source\"           : \""+f.getAbsolutePath()+"\"");
            f = new File("ae/images");
            lines.set(4, ", \"source_footage\"        : \""+f.getAbsolutePath()+"\"");
            f = new File("ae/output");
            lines.set(5, ", \"output_location\"       : \""+f.getAbsolutePath()+"\"");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("templater-options.json"))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    private static int writeToFile(String fp, int layers, List<Task> tl) {
        String header = "";
        for(int i = 0; i < layers; ++i) {
            header+="L"+(i+1)+"\t";
        }
        try {
            File f = new File(fp);
            if(!f.exists()) {
                return -1;
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(header);
            for(Task t : tl) {
                bw.newLine();
                bw.write(t.toString());
            }
            bw.close();
            return 0;
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int runner(String[] command) {
        try {
            Process process = new ProcessBuilder(command).start();
            int exitCode = process.waitFor();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void saveFile(String uploadDir, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file " + file.getOriginalFilename());
        }
        Path path = Paths.get(uploadDir);
        Files.copy(file.getInputStream(), path);
    }

    public void ffmpeg(String inputFilePath, String outputFilePath) {
        String[] command = {
            "ffmpeg",
            "-i", inputFilePath,
            "-filter_complex", "[0]reverse[r];[0][r]concat=n=2:v=1:a=0,fps=50",
            outputFilePath
    };

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    
    try {
        // Start the process
        Process process = processBuilder.start();

        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            log.info("Successfully generated gif");
            File f = new File(inputFilePath);
            f.delete();
        } else {
            log.error("FFmpeg instance failed @ AEServiceImpl.ffmpeg");
        }
    } catch (Exception e) {
        e.printStackTrace();}
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

	public ResponseEntity<Resource> getGif(String filename) {
        try {
            Path imagePath = Paths.get(VIDEO_DIR).resolve(filename).normalize();
            if (Files.exists(imagePath) && Files.isReadable(imagePath)) {
                Resource resource = new UrlResource(imagePath.toUri());
                
                if (resource.exists() && resource.isReadable()) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
                    headers.add(HttpHeaders.CONTENT_TYPE, "image/gif");
                    
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

    public Map<String, Integer> getTemplates() {
        try{BufferedReader br = new BufferedReader(new FileReader("ae/templates_list.tsv"));
        Map<String, Integer> lines = new HashMap<String, Integer>();
        String line;
        while((line=br.readLine()) != null) {
            String[] temp = line.split(" ");
            lines.put(temp[0], Integer.valueOf(temp[1]));
        }
        br.close();
        return lines;}
        catch(Exception e) {log.warn("Error getting template list. Returning null");return null;}
    }

    private static void emptyFolder(String folderPath) {
        Path directory = Paths.get(folderPath);

        try (Stream<Path> files = Files.list(directory)) {
            files.forEach(file -> {
                try {
                    Files.delete(file);
                    log.info("Cleaning output directory.");
                } catch (IOException e) {
                    log.error("Error with file permissions; unable to delete file");
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            log.error("An error occured when running the file deletion process");
            e.printStackTrace();
        }
    }

}
