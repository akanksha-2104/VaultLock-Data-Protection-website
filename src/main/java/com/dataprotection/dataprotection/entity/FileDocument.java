package com.dataprotection.dataprotection.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import com.dataprotection.dataprotection.enums.FileTag;

@Entity
@Table(name = "file_documents")
public class FileDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "file_document_seq")
    @SequenceGenerator(name = "file_document_seq", sequenceName = "file_document_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String storedFilename;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 150)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileTag tag = FileTag.PRIVATE;

    @ElementCollection
    @CollectionTable(name = "file_document_shared_emails", joinColumns = @JoinColumn(name = "file_document_id"))
    @Column(name = "shared_email", length = 120, nullable = false)
    private Set<String> sharedWithEmails = new HashSet<>();

    public FileTag getTag() {
        return tag;
    }

    public void setTag(FileTag tag) {
        this.tag = tag;
    }

    public Set<String> getSharedWithEmails() {
        return sharedWithEmails;
    }

    public void setSharedWithEmails(Set<String> sharedWithEmails) {
        this.sharedWithEmails = sharedWithEmails;
    }

    public Long getId() {
        return id;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
