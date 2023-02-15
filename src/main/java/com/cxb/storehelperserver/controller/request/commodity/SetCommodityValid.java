package com.cxb.storehelperserver.controller.request.commodity;

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
public class SetCommodityValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "商品错误")
    private int commid;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入商品编号")
    @Length(min = 2, message = "商品编号长度不能小于2个字符")
    @Length(max = 16, message = "商品编号长度不能大于16个字符")
    private String code;

    @NotEmpty(message = "请输入商品名称")
    @Length(min = 2, message = "商品名称长度不能小于2个字符")
    @Length(max = 16, message = "商品名称长度不能大于16个字符")
    private String name;

    @Min(value = 1, message = "商品品类错误")
    private int cid;

    @Min(value = 1, message = "商品单位错误")
    private int unit;

    @Length(max = 16, message = "商品备注长度不能大于16个字符")
    private String remark;

    @Size(max = 8, message = "商品属性不能超过8个")
    private List<String> attrs;
}
