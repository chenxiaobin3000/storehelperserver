package com.cxb.storehelperserver.controller.request.agreement;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/12
 */
@Data
public class ReturnValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "公司账号错误")
    private int gid;

    @Min(value = 1, message = "仓库账号错误")
    private int sid;

    @Size(min = 1, message = "商品id不能为空")
    private List<Integer> commoditys;

    @Size(min = 1, message = "商品数量不能为空")
    private List<Integer> values;

    @Size(min = 1, message = "商品价格不能为空")
    private List<String> prices;
}
