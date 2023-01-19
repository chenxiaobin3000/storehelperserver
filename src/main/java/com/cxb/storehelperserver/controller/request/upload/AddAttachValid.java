package com.cxb.storehelperserver.controller.request.upload;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/9
 */
@Data
public class AddAttachValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "类型错误")
    private int type;

    @NotEmpty(message = "请输入文件名")
    @Length(min = 4, message = "文件名长度不能小于4个字符")
    @Length(max = 16, message = "文件名长度不能大于16个字符")
    private String name;

    MultipartFile file;
}
