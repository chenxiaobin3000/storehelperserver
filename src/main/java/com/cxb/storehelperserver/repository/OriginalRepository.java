package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOriginalMapper;
import com.cxb.storehelperserver.model.TOriginal;
import com.cxb.storehelperserver.model.TOriginalExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 原料仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class OriginalRepository extends BaseRepository<TOriginal> {
    @Resource
    private TOriginalMapper originalMapper;

    public OriginalRepository() {
        init("ori::");
    }

    public TOriginal find(int id) {
        TOriginal original = getCache(id, TOriginal.class);
        if (null != original) {
            return original;
        }

        // 缓存没有就查询数据库
        original = originalMapper.selectByPrimaryKey(id);
        if (null != original) {
            setCache(id, original);
        }
        return original;
    }

    public TOriginal search(String name) {
        TOriginalExample example = new TOriginalExample();
        example.or().andNameLike("%" + name + "%");
        return originalMapper.selectOneByExample(example);
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TOriginalExample example = new TOriginalExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) originalMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TOriginalExample example = new TOriginalExample();
            example.or().andGidEqualTo(gid);
            total = (int) originalMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TOriginal> pagination(int gid, int page, int limit, String search) {
        TOriginalExample example = new TOriginalExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return originalMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在商品编号
     */
    public boolean checkCode(int gid, String code, int id) {
        TOriginalExample example = new TOriginalExample();
        example.or().andGidEqualTo(gid).andCodeEqualTo(code);
        if (0 == id) {
            return null != originalMapper.selectOneByExample(example);
        } else {
            TOriginal original = originalMapper.selectOneByExample(example);
            return null != original && !original.getId().equals(id);
        }
    }

    /*
     * desc: 判断公司是否存在商品名
     */
    public boolean checkName(int gid, String name, int id) {
        TOriginalExample example = new TOriginalExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != originalMapper.selectOneByExample(example);
        } else {
            TOriginal original = originalMapper.selectOneByExample(example);
            return null != original && !original.getId().equals(id);
        }
    }

    public boolean insert(TOriginal row) {
        if (originalMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TOriginal row) {
        if (originalMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TOriginal original = find(id);
        if (null == original) {
            return false;
        }
        delCache(id);
        delTotalCache(original.getGid());
        return originalMapper.deleteByPrimaryKey(id) > 0;
    }
}
