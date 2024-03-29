package com.cxb.storehelperserver.controller.request.role;

import com.cxb.storehelperserver.controller.request.IValid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/21
 */
@Data
public class SetRoleValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "角色账号错误")
    private int rid;

    @NotEmpty(message = "请输入角色名称")
    @Length(min = 2, message = "角色名称长度不能小于2个字符")
    @Length(max = 16, message = "角色名称长度不能大于16个字符")
    private String name;

    @NotEmpty(message = "请选择角色权限")
    @Size(min = 1, message = "请至少选择一个角色权限")
    private List<Integer> permissions;
}
