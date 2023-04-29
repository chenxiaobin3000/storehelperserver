package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.attribute.*;
import com.cxb.storehelperserver.model.TAttribute;
import com.cxb.storehelperserver.service.AttributeService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc: 属性接口
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@RestController
@RequestMapping("/api/attr")
public class AttributeController {
    @Resource
    private AttributeService attributeService;

    @PostMapping("/addAttr")
    public RestResult addAttribute(@Validated @RequestBody AddAttributeValid req) {
        TAttribute attribute = new TAttribute();
        attribute.setGid(req.getGid());
        attribute.setName(req.getName());
        return attributeService.addAttribute(req.getId(), attribute);
    }

    @PostMapping("/setAttr")
    public RestResult setAttribute(@Validated @RequestBody SetAttributeValid req) {
        TAttribute attribute = new TAttribute();
        attribute.setId(req.getAid());
        attribute.setGid(req.getGid());
        attribute.setName(req.getName());
        return attributeService.setAttribute(req.getId(), attribute);
    }

    @PostMapping("/delAttr")
    public RestResult delAttribute(@Validated @RequestBody DelAttributeValid req) {
        return attributeService.delAttribute(req.getId(), req.getAid());
    }

    @PostMapping("/getGroupAttr")
    public RestResult getGroupAttribute(@Validated @RequestBody GetGroupAttributeValid req) {
        return attributeService.getGroupAttribute(req.getId());
    }

    @PostMapping("/updateAttrTemp")
    public RestResult updateAttrTemplate(@Validated @RequestBody UpdateAttrTempValid req) {
        return attributeService.updateAttributeTemplate(req.getId(), req.getGid(), req.getTemplate());
    }

    @PostMapping("/getGroupAttrTemp")
    public RestResult getGroupAttribute(@Validated @RequestBody GetGroupAttrTemplateValid req) {
        return attributeService.getGroupAttributeTemplate(req.getId());
    }
}
