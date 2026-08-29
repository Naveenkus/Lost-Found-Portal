package com.example.lostoria.service;

import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {

    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<LostItem> getAll() {
        return lostItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LostItem getById(Long id) {
        return lostItemRepository.findById(id).orElse(null);
    }

    @Transactional
    public LostItem create(LostItem lostItem, MultipartFile imageFile) throws IOException {
        if (lostItem.getCreatedAt() == null) {
            lostItem.setCreatedAt(LocalDateTime.now());
        }
        LostItem savedItem = lostItemRepository.save(lostItem);

        if (imageFile != null && !imageFile.isEmpty()){
            Image image = Image.builder()
                    .imageName(imageFile.getOriginalFilename())
                    .imageDate(com.example.lostoria.util.ImageUtility.compress(imageFile.getBytes()))
                    .type(imageFile.getContentType())
                    .lostItem(savedItem)
                    .build();
            Image savedImage = imageRepository.save(image);
            savedItem.setImages(savedImage);
        }
        return savedItem;
    }

    public LostItem update(long id, LostItem newData) {
        return lostItemRepository.findById(id).map(item -> {
            item.setTitle(newData.getTitle());
            item.setDescription(newData.getDescription());
            item.setLocationLost(newData.getLocationLost());
            item.setStatus(newData.getStatus());
            item.setDatelost(newData.getDatelost());
            return lostItemRepository.save(item);
        }).orElseThrow(() -> new RuntimeException("Lost Item not found"));
    }

    public void delete(long id) {
        lostItemRepository.deleteById(id);
    }
}
