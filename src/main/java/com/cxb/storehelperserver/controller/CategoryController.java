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

    @PostMapping("/add")
    public RestResult add(@Validated @RequestBody AddCategoryValid req) {
        TCategory category = new TCategory();
        category.setName(req.getName());
        category.setParent(req.getParent());
        category.setLevel(req.getLevel());
        return categoryService.add(req.getId(), category);
    }

    @PostMapping("/set")
    public RestResult set(@Validated @RequestBody SetCategoryValid req) {
        TCategory category = new TCategory();
        category.setId(req.getCid());
        category.setName(req.getName());
        category.setParent(req.getParent());
        category.setLevel(req.getLevel());
        return categoryService.set(req.getId(), category);
    }

    @PostMapping("/del")
    public RestResult del(@Validated @RequestBody DelCategoryValid req) {
        return categoryService.del(req.getId(), req.getCid());
    }

    @PostMapping("/getList")
    public RestResult getList(@Validated @RequestBody GetGroupCategoryValid req) {
        return categoryService.getList(req.getId());
    }

    @PostMapping("/getTree")
    public RestResult getTree(@Validated @RequestBody GetGroupCategoryValid req) {
        return categoryService.getTree(req.getId());
    }
}
