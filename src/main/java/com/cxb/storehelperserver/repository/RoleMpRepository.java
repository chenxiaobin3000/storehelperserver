package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TRoleMpMapper;
import com.cxb.storehelperserver.model.TRoleMp;
import com.cxb.storehelperserver.model.TRoleMpExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 小程序角色仓库
 * auth: cxb
 * date: 2023/1/17
 */
@Slf4j
@Repository
public class RoleMpRepository extends BaseRepository<TRoleMp> {
    @Resource
    private TRoleMpMapper roleMpMapper;

    private final String cacheGroupName;

    public RoleMpRepository() {
        init("roleMp::");
        cacheGroupName = cacheName + "group::";
    }

    public TRoleMp find(int id) {
        TRoleMp roleMp = getCache(id, TRoleMp.class);
        if (null != roleMp) {
            return roleMp;
        }

        // 缓存没有就查询数据库
        roleMp = roleMpMapper.selectByPrimaryKey(id);
        if (null != roleMp) {
            setCache(id, roleMp);
        }
        return roleMp;
    }

    public List<TRoleMp> findByGroup(int gid) {
        List<TRoleMp> roleMps = List.class.cast(redisTemplate.opsForValue().get(cacheName + cacheGroupName + gid));
        if (null != roleMps) {
            return roleMps;
        }
        TRoleMpExample example = new TRoleMpExample();
        example.or().andGidEqualTo(gid);
        roleMps = roleMpMapper.selectByExample(example);
        if (null != roleMps) {
            redisTemplate.opsForValue().set(cacheName + cacheGroupName + gid, roleMps);
        }
        return roleMps;
    }

    /*
     * desc: 判断公司是否存在角色
     */
    public boolean check(int gid, String name, int id) {
        TRoleMpExample example = new TRoleMpExample();
        if (null == name) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameEqualTo(name);
        }
        if (0 == id) {
            return null != roleMpMapper.selectOneByExample(example);
        } else {
            TRoleMp roleMp = roleMpMapper.selectOneByExample(example);
            return null != roleMp && !roleMp.getId().equals(id);
        }
    }

    public List<TRoleMp> all(int gid, String search) {
        TRoleMpExample example = new TRoleMpExample();
        if (null == search || search.isEmpty()) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        return roleMpMapper.selectByExample(example);
    }

    public boolean insert(TRoleMp row) {
        if (roleMpMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TRoleMp row) {
        if (roleMpMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            delCache(cacheGroupName + row.getGid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TRoleMp roleMp = find(id);
        if (null == roleMp) {
            return false;
        }
        delCache(cacheGroupName + roleMp.getGid());
        delCache(id);
        return roleMpMapper.deleteByPrimaryKey(id) > 0;
    }
}
