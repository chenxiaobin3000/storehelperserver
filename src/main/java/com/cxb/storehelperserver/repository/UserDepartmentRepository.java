package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TUserDepartmentMapper;
import com.cxb.storehelperserver.model.TUserDepartment;
import com.cxb.storehelperserver.model.TUserDepartmentExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 用户部门仓库
 * auth: cxb
 * date: 2022/12/21
 */
@Slf4j
@Repository
public class UserDepartmentRepository extends BaseRepository<TUserDepartment> {
    @Resource
    private TUserDepartmentMapper userDepartmentMapper;

    public UserDepartmentRepository() {
        init("userDepartment::");
    }

    public TUserDepartment find(int uid) {
        TUserDepartment userDepartment = getCache(uid, TUserDepartment.class);
        if (null != userDepartment) {
            return userDepartment;
        }

        // 缓存没有就查询数据库
        TUserDepartmentExample example = new TUserDepartmentExample();
        example.or().andUidEqualTo(uid);
        userDepartment = userDepartmentMapper.selectOneByExample(example);
        if (null != userDepartment) {
            setCache(uid, userDepartment);
        }
        return userDepartment;
    }

    /*
     * desc: 判断公司是否存在部门
     */
    public boolean checkUser(int did) {
        TUserDepartmentExample example = new TUserDepartmentExample();
        example.or().andDidEqualTo(did);
        return null != userDepartmentMapper.selectOneByExample(example);
    }

    public boolean insert(int uid, int did) {
        TUserDepartment row = new TUserDepartment();
        row.setUid(uid);
        row.setDid(did);
        if (userDepartmentMapper.insert(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TUserDepartment row) {
        if (userDepartmentMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getUid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int uid) {
        delCache(uid);
        TUserDepartmentExample example = new TUserDepartmentExample();
        example.or().andUidEqualTo(uid);
        return userDepartmentMapper.deleteByExample(example) > 0;
    }
}
