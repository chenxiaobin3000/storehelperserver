package com.cxb.storehelperserver.controller.request.department;

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
public class SetDepartmentValid implements IValid {
    @Min(value = 1, message = "账号错误")
    private int id;

    @Min(value = 1, message = "部门信息错误")
    private int pid;

    @NotEmpty(message = "请输入部门名称")
    @Length(min = 1, message = "部门名称长度不能小于1个字符")
    @Length(max = 16, message = "部门名称长度不能大于16个字符")
    private String name;

    @Min(value = 0, message = "上级部门信息错误")
    private int parent;

    @Min(value = 1, message = "部门等级信息错误")
    private int level;
}
