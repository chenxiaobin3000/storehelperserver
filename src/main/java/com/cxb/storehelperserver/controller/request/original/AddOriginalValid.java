package com.cxb.storehelperserver.controller.request.original;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class AddOriginalValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入原料编号")
    @Length(min = 2, message = "原料编号长度不能小于2个字符")
    @Length(max = 16, message = "原料编号长度不能大于16个字符")
    private String code;

    @NotEmpty(message = "请输入原料名称")
    @Length(min = 2, message = "原料名称长度不能小于2个字符")
    @Length(max = 16, message = "原料名称长度不能大于16个字符")
    private String name;

    @Min(value = 1, message = "原料品类错误")
    private int cid;

    @Length(max = 16, message = "原料备注长度不能大于16个字符")
    private String remark;

    @Size(max = 8, message = "原料属性不能超过8个")
    private List<String> attrs;
}
