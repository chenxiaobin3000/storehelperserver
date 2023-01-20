package com.cxb.storehelperserver.controller;

import com.cxb.storehelperserver.controller.request.upload.AddAttachValid;
import com.cxb.storehelperserver.service.UploadService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.cxb.storehelperserver.util.TypeDefine.OrderType;

/**
 * desc: 上传接口
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Resource
    UploadService uploadService;

    @PostMapping("/addAttach")
    public RestResult addAttach(@Validated AddAttachValid req) {
        return uploadService.addAttach(req.getId(), OrderType.valueOf(req.getType()), req.getName(), req.getFile());
    }
}
