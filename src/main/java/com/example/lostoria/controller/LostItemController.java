package com.example.lostoria.controller;

import com.example.lostoria.model.LostItem;
import com.example.lostoria.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<LostItem> create(@RequestBody LostItem lostItem){
        return new ResponseEntity<>(lostItemService.create(lostItem), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable long id, @RequestBody LostItem lostItem){
        LostItem lostItem1 = null;
        lostItem1 = lostItemService.update(id, lostItem);
        if (lostItem != null){
            return new ResponseEntity<>("Updated", HttpStatus.OK);
        }else
            return new ResponseEntity<>("Failed to update", HttpStatus.BAD_REQUEST);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id){
        LostItem lostItem = lostItemService.getById(id);
        if (lostItem != null) {
            lostItemService.delete(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }else
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
    }
}
