package com.example.ForgeX.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

   
    List<UploadedImage> uploadImages(String path, List<MultipartFile> files) throws IOException;

   
    void deleteImage(String publicId);

    /** What Cloudinary gives back for one upload. publicId is needed to delete it later. */
    record UploadedImage(String url, String publicId) {
    }
}
