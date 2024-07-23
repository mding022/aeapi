package com.aeapi.springboot.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aeapi.springboot.models.Task;
import com.aeapi.springboot.service.AEService;

@Service
public class AEServiceImpl implements AEService{
    
    public int create(List<String> images) {
        List<Task> tasks = new ArrayList<Task>();
        Task testingTask = new Task();
        testingTask.setImages(images);
        tasks.add(testingTask);

        int er = env();
        if(er != 0) {
            System.out.println("Could not modify templater-optins.json file.");
            return -1;
        }
        Task t = new Task();
        t.setImages(images);
        List<Task> tl = new ArrayList<Task>();
        tl.add(t);

        er = writeToFile("ae/temp.tsv", images.size(), tl);

        runner(new String[] {"ae/./templater.sh", "-v", "2024", "-m"});
        
        return -1;
    }

    private static int env() {
        List<String> lines = new ArrayList<>();
        File json = new File("templater-options.json");
        if(!json.exists()) {
            System.out.println("Templater options file does not exist!");
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
        File f = new File("ae/Template.aep");
        if(f.exists()) {
            System.out.println("project file absolute path: " + f.getAbsolutePath());
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Exit Code: " + exitCode);
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
