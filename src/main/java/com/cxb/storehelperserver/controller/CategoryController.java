package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.category.*;
import com.cxb.storehelperserver.model.TCategory;
import com.cxb.storehelperserver.service.CategoryService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 品类接口
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @PostMapping("/addCategory")
    public RestResult addCategory(@Validated @RequestBody AddCategoryValid req) {
        TCategory category = new TCategory();
        category.setGid(req.getGid());
        category.setName(req.getName());
        category.setParent(req.getParent());
        category.setLevel(req.getLevel());
        return categoryService.addCategory(req.getId(), category);
    }

    @PostMapping("/setCategory")
    public RestResult setCategory(@Validated @RequestBody SetCategoryValid req) {
        TCategory category = new TCategory();
        category.setId(req.getCid());
        category.setGid(req.getGid());
        category.setName(req.getName());
        category.setParent(req.getParent());
        category.setLevel(req.getLevel());
        return categoryService.setCategory(req.getId(), category);
    }

    @PostMapping("/delCategory")
    public RestResult delCategory(@Validated @RequestBody DelCategoryValid req) {
        return categoryService.delCategory(req.getId(), req.getCid());
    }

    @PostMapping("/getGroupCategoryList")
    public RestResult getGroupCategoryList(@Validated @RequestBody GetGroupCategoryValid req) {
        return categoryService.getGroupCategoryList(req.getId());
    }

    @PostMapping("/getGroupCategoryTree")
    public RestResult getGroupCategoryTree(@Validated @RequestBody GetGroupCategoryValid req) {
        return categoryService.getGroupCategoryTree(req.getId());
    }
}
