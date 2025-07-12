package com.toeicify.toeic.controller;

import com.toeicify.toeic.service.MediaService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by hungpham on 7/11/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam(name = "folder", defaultValue = "uploads") String folder ) throws IOException {
        String key = mediaService.uploadFile(file, folder);
        return ResponseEntity.ok(key);
    }

    @GetMapping("/signed-url")
    @ApiMessage("Generate public url")
    public ResponseEntity<String> getSignedUrl(@RequestParam String key) {
        String url = mediaService.getSignedUrl(key);
        return ResponseEntity.ok(url);
    }

    @Hidden
    @DeleteMapping("/{filename}")
    @ApiMessage("Delete file")
    public ResponseEntity<Void> deleteFile(@PathVariable String filename) {
        mediaService.deleteFile(filename);
        return ResponseEntity.ok().build();
    }
}
