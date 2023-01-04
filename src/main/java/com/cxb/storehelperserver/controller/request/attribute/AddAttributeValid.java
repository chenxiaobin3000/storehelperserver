package com.cxb.storehelperserver.controller.request.attribute;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class AddAttributeValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入属性名称")
    @Length(min = 1, message = "属性名称长度不能小于1个字符")
    @Length(max = 8, message = "属性名称长度不能大于8个字符")
    private String name;

    @Min(value = 1, message = "属性索引信息错误")
    private int index;
}
