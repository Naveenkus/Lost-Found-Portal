package com.example.lostoria.controller;

import com.example.lostoria.model.LostItem;
import com.example.lostoria.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {
    @Autowired
    private LostItemService lostItemService;

    @GetMapping
    public ResponseEntity<List<LostItem>> getAll(){
        return new ResponseEntity<>(lostItemService.getAll(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<LostItem> getById(@PathVariable Long id){
        LostItem lostItem = lostItemService.getById(id);
        if(lostItem != null){
            return new ResponseEntity<>(lostItem, HttpStatus.OK);
        }else
            return new ResponseEntity<>(lostItem, HttpStatus.NOT_FOUND);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LostItem> create(
            @Valid @ModelAttribute LostItem lostItem,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        return new ResponseEntity<>(lostItemService.create(lostItem, imageFile), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable long id, @Valid @RequestBody LostItem lostItem){
        try {
            LostItem updated = lostItemService.update(id, lostItem);
            return new ResponseEntity<>("Updated", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id){
        try {
            lostItemService.delete(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
