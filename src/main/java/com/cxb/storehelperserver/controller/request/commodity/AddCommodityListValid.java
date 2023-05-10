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
public class AddCommodityListValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Size(min = 1, message = "商品编号不能为空")
    private List<String> codes;

    @Size(min = 1, message = "商品名称不能为空")
    private List<String> names;

    @Size(min = 1, message = "商品品类不能为空")
    private List<Integer> cids;

    private List<String> remarks;

    private List<String> storages;

    private List<String> accounts;

    private List<String> subs;

    @Min(value = 1, message = "商品属性数量不能为空")
    private int attr;

    private List<String> attr1;
    private List<String> attr2;
    private List<String> attr3;
    private List<String> attr4;
    private List<String> attr5;
    private List<String> attr6;
    private List<String> attr7;
    private List<String> attr8;
}
