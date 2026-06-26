package com.example.sprintsight.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.sprintsight.configurations.CloudinaryProperties;
import com.example.sprintsight.dtos.responses.ImageUploadResult;
import com.example.sprintsight.exceptions.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryImageService {
    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB

    public ImageUploadResult uploadAvatar(MultipartFile file) {
        return upload(file, properties.avatarsFolder(), 256);
    }

    public ImageUploadResult uploadProjectImage(MultipartFile file) {
        return upload(file, properties.projectImagesFolder(), 128);
    }

    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            log.warn("Could not derive Cloudinary public_id from URL: {}", imageUrl);
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
            log.info("Deleted Cloudinary asset: {}", publicId);
        } catch (IOException e) {
            // Don't fail the whole operation if cleanup fails — the new image
            // is already saved and the DB updated. Log it so orphaned assets
            // can be found and cleaned up later.
            log.error("Failed to delete Cloudinary asset {} — possible orphan", publicId, e);
        }
    }


    private ImageUploadResult upload(MultipartFile file, String folder, int dimension) {
        validate(file);

        String publicId = folder + "/" + UUID.randomUUID();

        Transformation<?> transformation = new Transformation<>()
                .width(dimension)
                .height(dimension)
                .crop("fill")
                .gravity("auto")
                .quality("auto")
                .fetchFormat("auto");

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image",
                            "transformation", transformation
                    )
            );

            String secureUrl = (String) result.get("secure_url");
            String returnedPublicId = (String) result.get("public_id");

            if (secureUrl == null) {
                throw new InvalidImageException("Upload succeeded but no URL was returned");
            }

            log.info("Uploaded image to Cloudinary: {}", returnedPublicId);
            return new ImageUploadResult(secureUrl, returnedPublicId);

        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new InvalidImageException("Failed to upload image. Please try again.", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("No file was provided");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new InvalidImageException("Image must be 5MB or smaller");
        }

        String detectedType;
        try {
            detectedType = tika.detect(file.getBytes());
        } catch (IOException e) {
            throw new InvalidImageException("Could not read the uploaded file", e);
        }

        if (!ALLOWED_TYPES.contains(detectedType)) {
            throw new InvalidImageException(
                    "Unsupported image type. Allowed: JPEG, PNG, WebP. Detected: " + detectedType);
        }
    }

    String extractPublicId(String url) {
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx < 0) return null;

        String afterUpload = url.substring(uploadIdx + "/upload/".length());

        if (afterUpload.matches("^v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }

        int dot = afterUpload.lastIndexOf('.');
        if (dot > 0) {
            afterUpload = afterUpload.substring(0, dot);
        }
        return afterUpload.isBlank() ? null : afterUpload;
    }
}
