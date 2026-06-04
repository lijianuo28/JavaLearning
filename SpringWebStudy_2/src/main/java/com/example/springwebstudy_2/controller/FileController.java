package com.example.springwebstudy_2.controller;

import jakarta.annotation.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/file")
public class FileController {
    private final String UPLOAD_DIR = "/home/yingxinli/JavaLearning/uploads/";
    @PostMapping("/upload")
    public String upload(@RequestParam("file")MultipartFile file) {
        if(file.isEmpty()){
            return "文件为空";
        }

        String originalFilename = file.getOriginalFilename();
        String savePath = UPLOAD_DIR + originalFilename;
        try {
            file.transferTo(new File(savePath));
            return "上传成功，保存路径：" + savePath;
        }
        catch (IOException e){
            e.printStackTrace();
            return "上传失败";
        }
    }


    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());
            if(resource.exists() && resource.isReadable()){
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + filename + "\"")
                        .body((Resource) resource);
            } else{
                return ResponseEntity.notFound().build();
            }
        } catch(MalformedURLException e){
            return ResponseEntity.badRequest().build();
        }
    }
}
