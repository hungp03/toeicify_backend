package com.toeicify.toeic.controller;

import com.toeicify.toeic.service.MediaService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by hungpham on 7/11/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping(value = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "folder", defaultValue = "uploads") String folder
    ) throws IOException {
        String key = mediaService.uploadFile(file, folder);
        return ResponseEntity.ok(key);
    }

    @PostMapping(
            value = "/upload/feedback",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<String>> uploadFeedbackAttachments(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of());
        }

        List<String> urls = mediaService.uploadFiles(files, "feedbacks");
        return ResponseEntity.ok(urls);
    }


//    @GetMapping("/signed-url")
//    @ApiMessage("Generate public url")
//    public ResponseEntity<String> getSignedUrl(@RequestParam String key) {
//        String url = mediaService.getSignedUrl(key);
//        return ResponseEntity.ok(url);
//    }

    @Hidden
    @DeleteMapping("/{filename}")
    @ApiMessage("Delete file")
    public ResponseEntity<Void> deleteFile(@PathVariable String filename) {
        mediaService.deleteFile(filename);
        return ResponseEntity.ok().build();
    }
}
