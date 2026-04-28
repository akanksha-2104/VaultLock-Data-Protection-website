package com.dataprotection.dataprotection.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dataprotection.dataprotection.dto.file.FileDownloadResponse;
import com.dataprotection.dataprotection.dto.file.FileMetadataResponse;
import com.dataprotection.dataprotection.dto.file.FileUploadResponse;
import com.dataprotection.dataprotection.entity.FileDocument;
import com.dataprotection.dataprotection.entity.User;
import com.dataprotection.dataprotection.enums.AuditAction;
import com.dataprotection.dataprotection.enums.FileTag;
import com.dataprotection.dataprotection.exception.AccessDeniedException;
import com.dataprotection.dataprotection.exception.FileStorageException;
import com.dataprotection.dataprotection.exception.ResourceNotFoundException;
import com.dataprotection.dataprotection.repository.FileDocumentRepository;
import com.dataprotection.dataprotection.repository.UserRepository;

@Service
public class FileService {

    private final FileDocumentRepository fileDocumentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final Path uploadRoot;
    private final Set<String> allowedExtensions;

    public FileService(FileDocumentRepository fileDocumentRepository,
            UserRepository userRepository,
            AuditLogService auditLogService,
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.storage.allowed-extensions}") String allowedExtensions) {
        this.fileDocumentRepository = fileDocumentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException ex) {
            throw new FileStorageException("Could not initialize upload directory", ex);
        }
    }

    @Transactional
    public FileUploadResponse store(MultipartFile file, String email, String ipAddress, String tagValue,
            String sharedWithEmailsValue) {
        validateFile(file);

        User user = getUserByEmail(email);
        FileTag tag = parseTag(tagValue);
        Set<String> sharedWithEmails = parseSharedWithEmails(sharedWithEmailsValue, email, tag);
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path destination = uploadRoot.resolve(storedFilename).normalize();

        if (!destination.startsWith(uploadRoot)) {
            throw new FileStorageException("Invalid file path");
        }

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file", ex);
        }

        FileDocument document = new FileDocument();
        document.setOwner(user);
        document.setOriginalFilename(originalFilename);
        document.setStoredFilename(storedFilename);
        document.setContentType(resolveContentType(file));
        document.setSize(file.getSize());
        document.setStoragePath(destination.toString());
        document.setUploadedAt(LocalDateTime.now());
        document.setTag(tag);
        document.setSharedWithEmails(sharedWithEmails);

        FileDocument savedDocument = fileDocumentRepository.save(document);
        auditLogService.log(user, AuditAction.UPLOAD, savedDocument.getOriginalFilename(), ipAddress);

        return new FileUploadResponse(
                savedDocument.getId(),
                savedDocument.getOriginalFilename(),
                savedDocument.getStoredFilename(),
                savedDocument.getContentType(),
                savedDocument.getSize(),
                savedDocument.getUploadedAt(),
                savedDocument.getTag().name(),
                Set.copyOf(savedDocument.getSharedWithEmails()),
                "File uploaded successfully",
                buildDownloadUrl(savedDocument.getId()),
                buildViewUrl(savedDocument.getId()),
                buildPublicDownloadUrl(savedDocument),
                buildPublicViewUrl(savedDocument));
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> listUserFiles(String email) {
        User user = getUserByEmail(email);
        return fileDocumentRepository.findAllByOwnerOrderByUploadedAtDesc(user).stream()
                .map(this::toMetadataResponse)
                .toList();
    }

    @Transactional
    public FileDownloadResponse download(Long fileId, String email, String ipAddress) {
        FileDocument document = getAccessibleDocument(fileId, email);
        auditLogService.log(document.getOwner(), AuditAction.DOWNLOAD, document.getOriginalFilename(), ipAddress);
        return new FileDownloadResponse(loadAsResource(document), document.getOriginalFilename(), document.getContentType());
    }

    @Transactional
    public FileDownloadResponse view(Long fileId, String email, String ipAddress) {
        FileDocument document = getAccessibleDocument(fileId, email);
        auditLogService.log(document.getOwner(), AuditAction.VIEW, document.getOriginalFilename(), ipAddress);
        return new FileDownloadResponse(loadAsResource(document), document.getOriginalFilename(), document.getContentType());
    }

    @Transactional
    public FileDownloadResponse downloadPublic(Long fileId, String ipAddress) {
        FileDocument document = getPublicDocument(fileId);
        auditLogService.log(document.getOwner(), AuditAction.DOWNLOAD, document.getOriginalFilename(), ipAddress);
        return new FileDownloadResponse(loadAsResource(document), document.getOriginalFilename(), document.getContentType());
    }

    @Transactional
    public FileDownloadResponse viewPublic(Long fileId, String ipAddress) {
        FileDocument document = getPublicDocument(fileId);
        auditLogService.log(document.getOwner(), AuditAction.VIEW, document.getOriginalFilename(), ipAddress);
        return new FileDownloadResponse(loadAsResource(document), document.getOriginalFilename(), document.getContentType());
    }

    @Transactional
    public void editFileMetadata(Long fileId, String email, String newFilename, String ipAddress) {
        FileDocument document = getOwnedDocument(fileId, email);
        document.setOriginalFilename(newFilename);
        fileDocumentRepository.save(document);
        auditLogService.log(document.getOwner(), AuditAction.EDIT, document.getOriginalFilename(), ipAddress);
    }

    @Transactional
    public void deleteFile(Long fileId, String email, String ipAddress) {
        FileDocument document = getOwnedDocument(fileId, email);
        fileDocumentRepository.delete(document);
        auditLogService.log(document.getOwner(), AuditAction.DELETE, document.getOriginalFilename(), ipAddress);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new FileStorageException("File name is required");
        }

        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        if (cleanedFilename.contains("..")) {
            throw new FileStorageException("Invalid file name");
        }

        String extension = getExtension(cleanedFilename);
        if (extension.isBlank() || !allowedExtensions.contains(extension)) {
            throw new FileStorageException("File type is not allowed");
        }
    }

    private FileDocument getOwnedDocument(Long fileId, String email) {
        FileDocument document = fileDocumentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (!document.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You are not allowed to access this file");
        }

        return document;
    }

    private FileDocument getAccessibleDocument(Long fileId, String email) {
        FileDocument document = fileDocumentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (document.getOwner().getEmail().equalsIgnoreCase(email)) {
            return document;
        }

        if (document.getTag() == FileTag.PUBLIC) {
            return document;
        }

        if (document.getTag() == FileTag.RESTRICTED
                && document.getSharedWithEmails().stream().anyMatch(sharedEmail -> sharedEmail.equalsIgnoreCase(email))) {
            return document;
        }

        throw new AccessDeniedException("You are not allowed to access this file");
    }

    private FileDocument getPublicDocument(Long fileId) {
        FileDocument document = fileDocumentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (document.getTag() != FileTag.PUBLIC) {
            throw new AccessDeniedException("This file is not public");
        }

        return document;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Resource loadAsResource(FileDocument document) {
        try {
            Path filePath = Paths.get(document.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Stored file is missing or unreadable");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Failed to read stored file", ex);
        }
    }

    private FileMetadataResponse toMetadataResponse(FileDocument document) {
        return new FileMetadataResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getStoredFilename(),
                document.getContentType(),
                document.getSize(),
                document.getUploadedAt(),
                document.getTag().name(),
                Set.copyOf(document.getSharedWithEmails()),
                buildDownloadUrl(document.getId()),
                buildViewUrl(document.getId()),
                buildPublicDownloadUrl(document),
                buildPublicViewUrl(document));
    }

    private String buildDownloadUrl(Long fileId) {
        return "/api/files/" + fileId + "/download";
    }

    private String buildViewUrl(Long fileId) {
        return "/api/files/" + fileId + "/view";
    }

    private String buildPublicDownloadUrl(FileDocument document) {
        return document.getTag() == FileTag.PUBLIC ? "/api/files/public/" + document.getId() + "/download" : null;
    }

    private String buildPublicViewUrl(FileDocument document) {
        return document.getTag() == FileTag.PUBLIC ? "/api/files/public/" + document.getId() + "/view" : null;
    }

    private FileTag parseTag(String tagValue) {
        try {
            return FileTag.valueOf((tagValue == null ? "PRIVATE" : tagValue.trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new FileStorageException("Invalid file tag");
        }
    }

    private Set<String> parseSharedWithEmails(String sharedWithEmailsValue, String ownerEmail, FileTag tag) {
        Set<String> sharedEmails = new LinkedHashSet<>();
        if (StringUtils.hasText(sharedWithEmailsValue)) {
            sharedEmails = Arrays.stream(sharedWithEmailsValue.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(String::toLowerCase)
                    .filter(email -> !email.equalsIgnoreCase(ownerEmail))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (tag == FileTag.RESTRICTED && sharedEmails.isEmpty()) {
            throw new FileStorageException("Restricted files must include at least one shared user email");
        }

        for (String sharedEmail : sharedEmails) {
            if (!userRepository.existsByEmail(sharedEmail)) {
                throw new FileStorageException("Shared user not found: " + sharedEmail);
            }
        }

        if (tag != FileTag.RESTRICTED) {
            return Set.of();
        }

        return sharedEmails;
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String resolveContentType(MultipartFile file) {
        if (StringUtils.hasText(file.getContentType())) {
            return file.getContentType();
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
