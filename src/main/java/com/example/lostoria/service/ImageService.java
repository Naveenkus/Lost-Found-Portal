package com.example.lostoria.service;

import com.example.lostoria.dto.ImageCacheDto;
import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.util.ImageUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepo;

    @CacheEvict(value = "images:data", key = "#result.imageID")
    @Transactional
    public Image saveImage(MultipartFile imageFile, LostItem lostItem, FoundItem foundItem) throws IOException {

        Image image = Image.builder()
                .imageName(imageFile.getOriginalFilename())
                .type(imageFile.getContentType())
                .imageDate(ImageUtility.compress(imageFile.getBytes()))
                .lostItem(lostItem)
                .foundItem(foundItem)
                .build();
        return imageRepo.save(image);
    }

    @Cacheable(value = "images:data", key = "#id")
    @Transactional(readOnly = true)
    public ImageCacheDto getCachedImage(long id) {
        Image image = imageRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
        byte[] decompressed = ImageUtility.decompressImage(image.getImageDate());
        return new ImageCacheDto(decompressed, image.getType());
    }
}
