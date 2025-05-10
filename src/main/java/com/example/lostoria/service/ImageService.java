package com.example.lostoria.service;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.util.ImageUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepo;


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

    public Optional<Image> getImageByName(String name) {
        return imageRepo.findByImageName(name);
    }
    public byte[] getImageData(String name) throws IOException {
        Optional<Image> dbImage = imageRepo.findByImageName(name);
        return ImageUtility.decompressImage(dbImage.get().getImageDate());
    }
}
