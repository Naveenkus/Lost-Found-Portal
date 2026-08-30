package com.example.lostoria.controller;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.service.FoundItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/found-items")
public class FoundItemController {
    @Autowired
    private FoundItemService foundItemService;

    @GetMapping
    public ResponseEntity<List<FoundItem>> getAll(){
        return new ResponseEntity<>(foundItemService.getAll(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<FoundItem> getById(@PathVariable long id){
        FoundItem foundItem = foundItemService.getById(id);
        if(foundItem != null)
            return new ResponseEntity<>(foundItem, HttpStatus.OK);
        else
            return new ResponseEntity<>(foundItem, HttpStatus.NOT_FOUND);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoundItem> createWithImage(
            @Valid @ModelAttribute FoundItem foundItem,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        return new ResponseEntity<>(foundItemService.create(foundItem, imageFile), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FoundItem> create(@Valid @RequestBody FoundItem foundItem){
        return new ResponseEntity<>(foundItemService.create(foundItem), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable long id, @Valid @RequestBody FoundItem foundItem){
        try {
            FoundItem updated = foundItemService.update(id, foundItem);
            return new ResponseEntity<>("Updated", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id){
        try {
            foundItemService.delete(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

}
