package com.toeicify.toeic.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by hungpham on 7/11/2025
 */
public interface MediaService {
    String uploadFile(MultipartFile file, String folder) throws IOException;

//    String getSignedUrl(String key);

    List<String> uploadFiles(List<MultipartFile> files, String folder) throws IOException;

    void deleteFile(String key);
}
