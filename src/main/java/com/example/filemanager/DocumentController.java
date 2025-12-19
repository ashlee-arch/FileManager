package com.example.filemanager;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class DocumentController {

    private final DocumentRepository documentRepository;

    // 업로드 저장 폴더 (프로젝트 루트/uploads)
    private final Path uploadDir = Paths.get("uploads");

    public DocumentController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("title") String title,
                         @RequestParam("file") MultipartFile file) throws IOException {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // 1) 원본 파일명 확보 (DB NOT NULL 컬럼 때문에 반드시 넣어야 함)
        String originalFilename = StringUtils.cleanPath(
                Optional.ofNullable(file.getOriginalFilename()).orElse("file")
        );

        // 2) 서버 저장용 파일명(충돌 방지용 UUID + 원본 확장자 유지)
        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot != -1 && dot < originalFilename.length() - 1) {
            ext = originalFilename.substring(dot); // ".hwp" 같은 확장자
        }
        String storedFilename = UUID.randomUUID() + ext;

        // 3) uploads 폴더 생성 + 파일 저장
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(storedFilename).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 4) DB 저장
        Document doc = new Document();
        doc.setTitle(title);
        doc.setOwnerUsername(username);

        // DB에는 "서버 저장 파일명"과 "원본 파일명"을 각각 저장
        doc.setFilename(storedFilename);
        doc.setOriginalFilename(originalFilename);

        documentRepository.save(doc);

        return "redirect:/?uploaded=1";
    }

    @GetMapping("/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다. id=" + id));

        // 본인 파일만 다운로드
        if (!username.equals(doc.getOwnerUsername())) {
            return ResponseEntity.status(403).build();
        }

        // 실제 파일은 uploads/{storedFilename}
        Path filePath = uploadDir.resolve(doc.getFilename()).normalize();

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }

        // Content-Type: probe 실패하면 octet-stream
        String contentType = Files.probeContentType(filePath);
        MediaType mediaType = (contentType != null)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        // 다운로드 파일명(원본 파일명) - UTF-8로 안전하게 처리
        String originalFilename = Optional.ofNullable(doc.getOriginalFilename())
                .orElse("download");

        // (중요) Content-Disposition이 제대로 들어가야 브라우저가 "1(2)" 같은 이름으로 저장하지 않음
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(originalFilename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) throws IOException {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다. id=" + id));

        // 본인 파일만 삭제
        if (!username.equals(doc.getOwnerUsername())) {
            return "redirect:/?error=forbidden";
        }

        // 실제 파일 삭제
        Path filePath = uploadDir.resolve(doc.getFilename()).normalize();
        Files.deleteIfExists(filePath);

        // DB 삭제
        documentRepository.delete(doc);

        return "redirect:/?deleted=1";
    }
}
