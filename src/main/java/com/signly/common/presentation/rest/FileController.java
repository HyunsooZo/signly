package com.signly.common.presentation.rest;

import com.signly.common.storage.FileStorageService;
import com.signly.common.storage.StoredFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<StoredFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category) {

        StoredFile storedFile = fileStorageService.storeFile(file, category);
        return ResponseEntity.ok(storedFile);
    }

    @GetMapping("/download/{category}/{filename}")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable String category,
            @PathVariable String filename) {

        String filePath = category + "/" + filename;
        byte[] fileData = fileStorageService.loadFile(filePath);

        ByteArrayResource resource = new ByteArrayResource(fileData);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                   URLEncoder.encode(filename, StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileData.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/view/{category}/{filename}")
    public ResponseEntity<ByteArrayResource> viewFile(
            @PathVariable String category,
            @PathVariable String filename) {

        String filePath = category + "/" + filename;
        byte[] fileData = fileStorageService.loadFile(filePath);

        ByteArrayResource resource = new ByteArrayResource(fileData);

        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .contentLength(fileData.length)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @DeleteMapping("/{category}/{filename}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String category,
            @PathVariable String filename) {

        String filePath = category + "/" + filename;
        fileStorageService.deleteFile(filePath);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists/{category}/{filename}")
    public ResponseEntity<Boolean> fileExists(
            @PathVariable String category,
            @PathVariable String filename) {

        String filePath = category + "/" + filename;
        boolean exists = fileStorageService.fileExists(filePath);
        return ResponseEntity.ok(exists);
    }

    private String determineContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case ".pdf" -> "application/pdf";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}