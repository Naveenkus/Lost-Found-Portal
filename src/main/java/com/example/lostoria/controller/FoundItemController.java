package com.example.lostoria.controller;

import com.example.lostoria.model.FoundItem;
import com.example.lostoria.service.FoundItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<FoundItem> create(@RequestBody FoundItem foundItem){
        return new ResponseEntity<>(foundItemService.create(foundItem), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable long id, @RequestBody FoundItem foundItem){
        FoundItem foundItem1 = null;
        foundItem1 = foundItemService.update(id, foundItem);
        if (foundItem != null)
            return new ResponseEntity<>("Updated", HttpStatus.OK);
        else
            return new ResponseEntity<>("Failed to update", HttpStatus.BAD_REQUEST);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id){
        FoundItem foundItem = foundItemService.getById(id);
        if (foundItem != null){
            foundItemService.delete(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }else
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
    }

}
