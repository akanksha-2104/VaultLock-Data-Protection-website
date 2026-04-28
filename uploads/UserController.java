package com.dataprotection.dataprotection.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dataprotection.dataprotection.model.User;
import com.dataprotection.dataprotection.service.UserService;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService service;

    // 🔹 Test API (use this to check if controller is working)
    @GetMapping("/test")
    public String test() {
        return "WORKING";
    }

    // 🔹 Register API
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return service.register(user);
    }

    // 🔹 Login API
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        return service.login(user.getEmail(), user.getPassword());
    }

    // 🔹 Upload File API
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return "File is empty!";
            }

            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = uploadDir + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            return "File uploaded successfully: " + file.getOriginalFilename();

        } catch (IOException e) {
            e.printStackTrace();
            return "Upload failed!";
        }
    }

    // 🔹 Download File API
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {

        try {
            String filePath = System.getProperty("user.dir") + "/uploads/" + filename;
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toURI());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
