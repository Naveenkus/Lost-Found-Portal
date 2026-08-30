package com.example.lostoria.service;

import com.example.lostoria.dto.UserPrincipal;
import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.repository.LostItemRepository;
import com.example.lostoria.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            lostItem.setReportedBy(userPrincipal.getUser());
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

    private void verifyOwnershipOrAdmin(LostItem item, String action) {
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

    public LostItem update(long id, LostItem newData) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lost Item not found"));
        verifyOwnershipOrAdmin(item, "update");

        item.setTitle(newData.getTitle());
        item.setDescription(newData.getDescription());
        item.setLocationLost(newData.getLocationLost());
        item.setStatus(newData.getStatus());
        item.setDatelost(newData.getDatelost());
        return lostItemRepository.save(item);
    }

    public void delete(long id) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lost Item not found"));
        verifyOwnershipOrAdmin(item, "delete");

        lostItemRepository.deleteById(id);
    }
}
