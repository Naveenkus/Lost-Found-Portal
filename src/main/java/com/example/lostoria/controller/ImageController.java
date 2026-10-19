package com.example.lostoria.controller;

import com.example.lostoria.dto.ImageCacheDto;
import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.service.FoundItemService;
import com.example.lostoria.service.ImageService;
import com.example.lostoria.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;
    @Autowired
    private LostItemService lostItemService;
    @Autowired
    private FoundItemService foundItemService;

    @Autowired
    private CacheManager cacheManager;

    @PostMapping("/upload/lost/{lostItemId}")
    public ResponseEntity<String> uploadImageForLostItem(@PathVariable long lostItemId, @RequestParam("image") MultipartFile imageFile) throws IOException {
        LostItem lostItem = lostItemService.getById(lostItemId);
        if (lostItem == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lost item not found");
        }
        imageService.saveImage(imageFile, lostItem, null);

        evictCache("lost-items:all", "all");
        evictCache("lost-items:single", lostItemId);

        return ResponseEntity.status(HttpStatus.OK).body("Image uploaded successfully");
    }


    @PostMapping("/upload/found/{foundItemId}")
    public ResponseEntity<String> uploadImageForFoundItem(@PathVariable long foundItemId, @RequestParam("image") MultipartFile imageFile) throws IOException {
        FoundItem foundItem = foundItemService.getById(foundItemId);
        if (foundItem == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No items found");
        }
        imageService.saveImage(imageFile, null, foundItem);

        evictCache("found-items:all", "all");
        evictCache("found-items:single", foundItemId);

        return ResponseEntity.status(HttpStatus.OK).body("Image uploaded successfully");
    }

    @GetMapping("/view/id/{imageId}")
    public ResponseEntity<byte[]> getImageById(@PathVariable long imageId) {
        ImageCacheDto cached = imageService.getCachedImage(imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(cached.getContentType()))
                .body(cached.getData());
    }

    private void evictCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.evict(key);
        }
    }
}
