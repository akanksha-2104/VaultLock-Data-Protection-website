package com.dataprotection.dataprotection.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dataprotection.dataprotection.dto.file.FileDownloadResponse;
import com.dataprotection.dataprotection.dto.file.FileMetadataResponse;
import com.dataprotection.dataprotection.dto.file.FileUploadResponse;
import com.dataprotection.dataprotection.service.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "tag", defaultValue = "PRIVATE") String tag,
            @RequestParam(value = "sharedWithEmails", required = false) String sharedWithEmails,
            Authentication authentication, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                fileService.store(file, authentication.getName(), httpRequest.getRemoteAddr(), tag, sharedWithEmails));
    }

    @GetMapping
    public ResponseEntity<List<FileMetadataResponse>> listFiles(Authentication authentication) {
        return ResponseEntity.ok(fileService.listUserFiles(authentication.getName()));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, Authentication authentication,
            HttpServletRequest httpRequest) {
        FileDownloadResponse response = fileService.download(fileId, authentication.getName(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + response.originalFilename() + "\"")
                .body(response.resource());
    }

    @GetMapping("/{fileId}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId, Authentication authentication,
            HttpServletRequest httpRequest) {
        FileDownloadResponse response = fileService.view(fileId, authentication.getName(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + response.originalFilename() + "\"")
                .body(response.resource());
    }

    @GetMapping("/public/{fileId}/download")
    public ResponseEntity<Resource> downloadPublicFile(@PathVariable Long fileId, HttpServletRequest httpRequest) {
        FileDownloadResponse response = fileService.downloadPublic(fileId, httpRequest.getRemoteAddr());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + response.originalFilename() + "\"")
                .body(response.resource());
    }

    @GetMapping("/public/{fileId}/view")
    public ResponseEntity<Resource> viewPublicFile(@PathVariable Long fileId, HttpServletRequest httpRequest) {
        FileDownloadResponse response = fileService.viewPublic(fileId, httpRequest.getRemoteAddr());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + response.originalFilename() + "\"")
                .body(response.resource());
    }
}
