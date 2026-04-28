package com.dataprotection.dataprotection.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dataprotection.dataprotection.entity.FileDocument;
import com.dataprotection.dataprotection.entity.User;
import com.dataprotection.dataprotection.enums.FileTag;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {

    List<FileDocument> findAllByOwnerOrderByUploadedAtDesc(User owner);

    List<FileDocument> findAllByTagOrderByUploadedAtDesc(FileTag tag);
}
