package com.example.lostoria.service;

import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {

    @Autowired
    private LostItemRepository lostItemRepository;
    public List<LostItem> getAll() {
        return lostItemRepository.findAll();
    }
    public LostItem getById(Long id) {
        return lostItemRepository.findById(id).orElse(null);
    }
    public LostItem create(LostItem lostItem) {
        return lostItemRepository.save(lostItem);
    }

    public LostItem update(long id, LostItem newData) {
        return lostItemRepository.findById(id).map(item -> {
            item.setTitle(newData.getTitle());
            item.setDescription(newData.getDescription());
            item.setLocationLost(newData.getLocationLost());
            item.setImageUrl(newData.getImageUrl());
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
