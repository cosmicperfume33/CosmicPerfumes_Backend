package com.example.ForgeX.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final String DEFAULT_FOLDER = "forgex/products";
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    public FileServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public List<UploadedImage> uploadImages(String path, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }

        String folder = (path == null || path.isBlank()) ? DEFAULT_FOLDER : path;
        List<UploadedImage> uploaded = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                validate(file);

                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder,
                                "resource_type", "image"
                        )
                );

                uploaded.add(new UploadedImage(
                        String.valueOf(result.get("secure_url")),
                        String.valueOf(result.get("public_id"))
                ));
            }
        } catch (RuntimeException | IOException e) {
            for (UploadedImage orphan : uploaded) {
                deleteImage(orphan.publicId());
            }
            throw e;
        }

        return uploaded;
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank() || "null".equals(publicId)) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        } catch (Exception e) {
            
            log.warn("Could not delete {} from Cloudinary; it is now orphaned", publicId, e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image must not be empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Each image must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Images must be JPEG, PNG or WebP");
        }
    }
}
