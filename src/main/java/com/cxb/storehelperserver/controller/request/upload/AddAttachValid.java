package com.cxb.storehelperserver.controller.request.upload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/9
 */
@Data
public class AddAttachValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "类型错误")
    private int type;

    @NotEmpty(message = "请添加附件")
    MultipartFile files[];
}
