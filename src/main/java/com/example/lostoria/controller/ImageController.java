package com.example.lostoria.controller;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.Image;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.service.FoundItemService;
import com.example.lostoria.service.ImageService;
import com.example.lostoria.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;
    @Autowired
    private LostItemService lostItemService;
    @Autowired
    private FoundItemService foundItemService;

    @PostMapping("/upload/lost/{lostItemId}")
    public ResponseEntity<String> uploadImageForLostItem(@PathVariable long lostItemId, @RequestParam("image") MultipartFile imageFile) throws IOException {
        Optional<LostItem> lostItemOpt = Optional.ofNullable(lostItemService.getById(lostItemId));
        if (!lostItemOpt.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lost item not found");
        }
        imageService.saveImage(imageFile, lostItemOpt.get(), null);
        return ResponseEntity.status(HttpStatus.OK).body("Image uploaded successfully");
    }
    @PostMapping("/upload/found/{foundItemId}")
    public ResponseEntity<String> uploadImageForFoundItem(@PathVariable long foundItemId, @RequestParam("image") MultipartFile imageFile) throws IOException {
        Optional<FoundItem> foundItemOpt = Optional.ofNullable(foundItemService.getById(foundItemId));
        if (!foundItemOpt.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No items found");
        }
        imageService.saveImage(imageFile,null, foundItemOpt.get());
        return ResponseEntity.status(HttpStatus.OK).body("Image uploaded successfully");
    }

    @GetMapping("/view/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) throws IOException {
        Optional<Image> imageOpt = imageService.getImageByName(imageName);
        if (!imageOpt.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        byte[] imageData = imageService.getImageData(imageName);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(imageOpt.get().getType()))
                .body(imageData);
    }
}
