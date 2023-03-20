package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TDepartmentMapper;
import com.cxb.storehelperserver.model.TDepartment;
import com.cxb.storehelperserver.model.TDepartmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 部门仓库
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Repository
public class DepartmentRepository extends BaseRepository<TDepartment> {
    @Resource
    private TDepartmentMapper departmentMapper;

    private final String cacheGroupName;

    public DepartmentRepository() {
        init("depart::");
        cacheGroupName = cacheName + "group::";
    }

    public TDepartment find(int id) {
        TDepartment department = getCache(id, TDepartment.class);
        if (null != department) {
            return department;
        }

        // 缓存没有就查询数据库
        department = departmentMapper.selectByPrimaryKey(id);
        if (null != department) {
            setCache(id, department);
        }
        return department;
    }

    public List<TDepartment> findByGroup(int gid) {
        List<TDepartment> departments = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != departments) {
            return departments;
        }
        TDepartmentExample example = new TDepartmentExample();
        example.or().andGidEqualTo(gid);
        example.setOrderByClause("level asc");
        departments = departmentMapper.selectByExample(example);
        if (null != departments) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, departments);
        }
        return departments;
    }

    /*
     * desc: 判断公司是否存在部门
     */
    public boolean check(int gid, String name, int id) {
        TDepartmentExample example = new TDepartmentExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != departmentMapper.selectOneByExample(example);
        } else {
            TDepartment category = departmentMapper.selectOneByExample(example);
            return null != category && !category.getId().equals(id);
        }
    }

    public boolean insert(TDepartment row) {
        if (departmentMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TDepartment row) {
        if (departmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TDepartment department = find(id);
        if (null == department) {
            return false;
        }
        delCache(cacheGroupName + department.getGid());
        delCache(id);
        return departmentMapper.deleteByPrimaryKey(id) > 0;
    }

    public boolean checkChildren(int id) {
        TDepartmentExample example = new TDepartmentExample();
        example.or().andParentEqualTo(id);
        TDepartment department = departmentMapper.selectOneByExample(example);
        return null != department;
    }
}
