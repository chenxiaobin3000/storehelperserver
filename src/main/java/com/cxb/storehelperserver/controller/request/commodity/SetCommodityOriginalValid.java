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
public class SetCommodityOriginalValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "商品错误")
    private int cid;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @NotEmpty(message = "请输入原料编号")
    @Length(min = 2, message = "原料编号长度不能小于2个字符")
    @Length(max = 16, message = "原料编号长度不能大于16个字符")
    private String oid;
}
