package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.attribute.AddAttributeValid;
import com.cxb.storehelperserver.controller.request.attribute.DelAttributeValid;
import com.cxb.storehelperserver.controller.request.attribute.GetGroupAttributeValid;
import com.cxb.storehelperserver.controller.request.attribute.SetAttributeValid;
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
@RequestMapping("/api/attribute")
public class AttributeController {
    @Resource
    private AttributeService attributeService;

    @PostMapping("/addAttribute")
    public RestResult addAttribute(@Validated @RequestBody AddAttributeValid req) {
        TAttribute attribute = new TAttribute();
        attribute.setGid(req.getGid());
        attribute.setName(req.getName());
        attribute.setIndex(req.getIndex());
        return attributeService.addAttribute(req.getId(), attribute);
    }

    @PostMapping("/setAttribute")
    public RestResult setAttribute(@Validated @RequestBody SetAttributeValid req) {
        TAttribute attribute = new TAttribute();
        attribute.setId(req.getAid());
        attribute.setGid(req.getGid());
        attribute.setName(req.getName());
        attribute.setIndex(req.getIndex());
        return attributeService.setAttribute(req.getId(), attribute);
    }

    @PostMapping("/delAttribute")
    public RestResult delAttribute(@Validated @RequestBody DelAttributeValid req) {
        return attributeService.delAttribute(req.getId(), req.getAid());
    }

    @PostMapping("/getGroupAttribute")
    public RestResult getGroupAttribute(@Validated @RequestBody GetGroupAttributeValid req) {
        return attributeService.getGroupAttribute(req.getId());
    }
}
