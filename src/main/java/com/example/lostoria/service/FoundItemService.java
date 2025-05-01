package com.example.lostoria.service;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.repository.FoundItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoundItemService {
    @Autowired
    private FoundItemRepository foundItemRepository;


    public List<FoundItem> getAll() {
        return foundItemRepository.findAll();
    }

    public FoundItem getById(long id) {
        return foundItemRepository.findById(id).orElse(null);
    }

    public FoundItem create(FoundItem foundItem) {
        return foundItemRepository.save(foundItem);
    }

    public FoundItem update(long id, FoundItem newData) {
        return foundItemRepository.findById(id).map(item -> {
            item.setTitle(newData.getTitle());
            item.setDescription(newData.getDescription());
            item.setLocationFound(newData.getLocationFound());
            item.setImageUrl(newData.getImageUrl());
            item.setStatus(newData.getStatus());
            item.setCreatedAt(newData.getCreatedAt());
            item.setDateFound(newData.getDateFound());
            return foundItemRepository.save(item);
        }).orElseThrow(() -> new RuntimeException(" No Item not found"));
    }

    public void delete(long id) {
        foundItemRepository.deleteById(id);
    }
}
