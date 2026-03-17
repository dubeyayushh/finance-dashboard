package com.finance.controller;

import com.finance.model.Category;
import com.finance.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // GET /api/categories
    // Returns all categories — used to populate dropdowns in the frontend
    // Note: calling repository directly from controller is acceptable
    // for simple read-only operations with no business logic
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}