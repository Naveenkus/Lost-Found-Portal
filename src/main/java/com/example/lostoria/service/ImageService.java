package com.example.lostoria.service;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.util.ImageUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepo;

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

    @Transactional(readOnly = true)
    public Optional<Image> getImageById(long id) {
        return imageRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public byte[] getImageDataById(long id) {
        Image image = imageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        return ImageUtility.decompressImage(image.getImageDate());
    }
}
