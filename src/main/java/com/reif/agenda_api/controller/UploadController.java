package com.reif.agenda_api.controller;

import com.reif.agenda_api.service.ImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    private static final Set<String> PASTAS_PROFISSIONAL = Set.of("portfolio", "services", "profiles");

    private final ImageStorageService imageStorageService;

    public UploadController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /** Público: o cliente envia a inspiração antes de ter conta (RF05). */
    @PostMapping(value = "/inspiration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadInspiration(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", imageStorageService.upload(file, "inspiration")));
    }

    /** Autenticado: portfolio, services ou profiles. */
    @PostMapping(value = "/{pasta}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@PathVariable String pasta,
                                                      @RequestParam("file") MultipartFile file) {
        if (!PASTAS_PROFISSIONAL.contains(pasta)) {
            throw new IllegalArgumentException("Pasta de upload inválida.");
        }
        return ResponseEntity.ok(Map.of("url", imageStorageService.upload(file, pasta)));
    }
}