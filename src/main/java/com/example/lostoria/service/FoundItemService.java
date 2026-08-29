package com.example.lostoria.service;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.Image;
import com.example.lostoria.repository.FoundItemRepository;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.util.ImageUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoundItemService {
    @Autowired
    private FoundItemRepository foundItemRepository;
    @Autowired
    private ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<FoundItem> getAll() {
        return foundItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FoundItem getById(long id) {
        return foundItemRepository.findById(id).orElse(null);
    }

    public FoundItem create(FoundItem foundItem) {
        if (foundItem.getCreatedAt() == null) {
            foundItem.setCreatedAt(LocalDateTime.now());
        }
        return foundItemRepository.save(foundItem);
    }

    @Transactional
    public FoundItem create(FoundItem foundItem, MultipartFile imageFile) throws IOException {
        if (foundItem.getCreatedAt() == null) {
            foundItem.setCreatedAt(LocalDateTime.now());
        }
        FoundItem savedItem = foundItemRepository.save(foundItem);
        if (imageFile != null && !imageFile.isEmpty()) {
            Image image = Image.builder()
                    .imageName(imageFile.getOriginalFilename())
                    .type(imageFile.getContentType())
                    .imageDate(ImageUtility.compress(imageFile.getBytes()))
                    .foundItem(savedItem)
                    .build();
            Image savedImage = imageRepository.save(image);
            savedItem.getImages().add(savedImage);
        }
        return savedItem;
    }

    public FoundItem update(long id, FoundItem newData) {
        return foundItemRepository.findById(id).map(item -> {
            item.setTitle(newData.getTitle());
            item.setDescription(newData.getDescription());
            item.setLocationFound(newData.getLocationFound());
            item.setImageUrl(newData.getImageUrl());
            item.setStatus(newData.getStatus());
            item.setDateFound(newData.getDateFound());
            return foundItemRepository.save(item);
        }).orElseThrow(() -> new RuntimeException("Found Item not found"));
    }

    public void delete(long id) {
        foundItemRepository.deleteById(id);
    }
}

