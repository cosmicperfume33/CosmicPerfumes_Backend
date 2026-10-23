package com.example.ForgeX.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * Uploads every file and returns one result per file, in the order given.
     * If any file fails, the ones already uploaded are removed again, so a
     * half-finished batch never leaves orphans on Cloudinary.
     */
    List<UploadedImage> uploadImages(String path, List<MultipartFile> files) throws IOException;

    /** Removes a file from Cloudinary. Safe to call with an unknown id. */
    void deleteImage(String publicId);

    /** What Cloudinary gives back for one upload. publicId is needed to delete it later. */
    record UploadedImage(String url, String publicId) {
    }
}
