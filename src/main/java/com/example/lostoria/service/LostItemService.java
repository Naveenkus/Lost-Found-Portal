package com.example.lostoria.service;

import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {

    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private ImageRepository imageRepository;
    public List<LostItem> getAll() {
        return lostItemRepository.findAll();
    }
    public LostItem getById(Long id) {
        return lostItemRepository.findById(id).orElse(null);
    }
    public LostItem create(LostItem lostItem, MultipartFile imageFile) throws IOException {

//        MultipartFile imageFile = (MultipartFile) lostItem.getImages();
        if (imageFile != null && !imageFile.isEmpty()){
            Image image = new Image();
            image.setImageName(imageFile.getOriginalFilename());
            image.setImageDate(imageFile.getBytes());
            image.setType(imageFile.getContentType());
            image.setLostItem(lostItem);
            imageRepository.save(image);
        }
        return lostItemRepository.save(lostItem);
    }

    public LostItem update(long id, LostItem newData) {
        return lostItemRepository.findById(id).map(item -> {
            item.setTitle(newData.getTitle());
            item.setDescription(newData.getDescription());
            item.setLocationLost(newData.getLocationLost());
//            item.setImageUrl(newData.getImageUrl());
            item.setStatus(newData.getStatus());
            item.setCreatedAt(newData.getCreatedAt());
            item.setDatelost(newData.getDatelost());
            return lostItemRepository.save(item);
        }).orElseThrow(() -> new RuntimeException("Lost Item not found"));
    }

    public void delete(long id) {
        lostItemRepository.deleteById(id);
    }
}
