package com.cxb.storehelperserver.controller.request.destroy;

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
public class SetDestroyValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "废品错误")
    private int destid;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入废品编号")
    @Length(min = 2, message = "废品编号长度不能小于2个字符")
    @Length(max = 16, message = "废品编号长度不能大于16个字符")
    private String code;

    @NotEmpty(message = "请输入废品名称")
    @Length(min = 2, message = "废品名称长度不能小于2个字符")
    @Length(max = 16, message = "废品名称长度不能大于16个字符")
    private String name;

    @Min(value = 1, message = "废品属性模板错误")
    private int atid;

    @Min(value = 1, message = "废品品类错误")
    private int cid;

    @Min(value = 1, message = "废品价格错误")
    private BigDecimal price;

    @Min(value = 1, message = "废品单位错误")
    private int unit;

    @Length(max = 16, message = "废品备注长度不能大于16个字符")
    private String remark;

    @Size(max = 8, message = "废品属性不能超过8个")
    private List<String> attrs;
}
