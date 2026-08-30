package com.example.lostoria.service;

import com.example.lostoria.dto.UserPrincipal;
import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.Image;
import com.example.lostoria.repository.FoundItemRepository;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.util.ImageUtility;
import com.example.lostoria.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            foundItem.setReportedBy(userPrincipal.getUser());
        }
        return foundItemRepository.save(foundItem);
    }

    @Transactional
    public FoundItem create(FoundItem foundItem, MultipartFile imageFile) throws IOException {
        if (foundItem.getCreatedAt() == null) {
            foundItem.setCreatedAt(LocalDateTime.now());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            foundItem.setReportedBy(userPrincipal.getUser());
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

    private void verifyOwnershipOrAdmin(FoundItem item, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authenticated");
        }
        boolean isAdmin = userPrincipal.getUser() != null && userPrincipal.getUser().getRole() == Role.ADMIN;
        boolean isOwner = item.getReportedBy() != null
                && userPrincipal.getUser() != null
                && item.getReportedBy().getId() != null
                && item.getReportedBy().getId().equals(userPrincipal.getUser().getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to " + action + " this item");
        }
    }

    public FoundItem update(long id, FoundItem newData) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Found Item not found"));
        verifyOwnershipOrAdmin(item, "update");

        item.setTitle(newData.getTitle());
        item.setDescription(newData.getDescription());
        item.setLocationFound(newData.getLocationFound());
        item.setImageUrl(newData.getImageUrl());
        item.setStatus(newData.getStatus());
        item.setDateFound(newData.getDateFound());
        return foundItemRepository.save(item);
    }

    public void delete(long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Found Item not found"));
        verifyOwnershipOrAdmin(item, "delete");

        foundItemRepository.deleteById(id);
    }
}

