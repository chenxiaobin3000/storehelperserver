package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TDestroyMapper;
import com.cxb.storehelperserver.model.TDestroy;
import com.cxb.storehelperserver.model.TDestroyExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 废料仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class DestroyRepository extends BaseRepository<TDestroy> {
    @Resource
    private TDestroyMapper destroyMapper;

    public DestroyRepository() {
        init("dest::");
    }

    public TDestroy find(int id) {
        TDestroy destroy = getCache(id, TDestroy.class);
        if (null != destroy) {
            return destroy;
        }

        // 缓存没有就查询数据库
        destroy = destroyMapper.selectByPrimaryKey(id);
        if (null != destroy) {
            setCache(id, destroy);
        }
        return destroy;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TDestroyExample example = new TDestroyExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) destroyMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TDestroyExample example = new TDestroyExample();
            example.or().andGidEqualTo(gid);
            total = (int) destroyMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TDestroy> pagination(int gid, int page, int limit, String search) {
        TDestroyExample example = new TDestroyExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return destroyMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在商品编号
     */
    public boolean checkCode(int gid, String code, int id) {
        TDestroyExample example = new TDestroyExample();
        example.or().andGidEqualTo(gid).andCodeEqualTo(code);
        if (0 == id) {
            return null != destroyMapper.selectOneByExample(example);
        } else {
            TDestroy destroy = destroyMapper.selectOneByExample(example);
            return null != destroy && !destroy.getId().equals(id);
        }
    }

    /*
     * desc: 判断公司是否存在商品名
     */
    public boolean checkName(int gid, String name, int id) {
        TDestroyExample example = new TDestroyExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != destroyMapper.selectOneByExample(example);
        } else {
            TDestroy destroy = destroyMapper.selectOneByExample(example);
            return null != destroy && !destroy.getId().equals(id);
        }
    }

    public boolean insert(TDestroy row) {
        if (destroyMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TDestroy row) {
        if (destroyMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TDestroy destroy = find(id);
        if (null == destroy) {
            return false;
        }
        delCache(id);
        delTotalCache(destroy.getGid());
        return destroyMapper.deleteByPrimaryKey(id) > 0;
    }
}
