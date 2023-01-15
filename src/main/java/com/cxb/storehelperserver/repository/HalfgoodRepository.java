package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.THalfgoodMapper;
import com.cxb.storehelperserver.model.THalfgood;
import com.cxb.storehelperserver.model.THalfgoodExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 半成品仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class HalfgoodRepository extends BaseRepository<THalfgood> {
    @Resource
    private THalfgoodMapper halfgoodMapper;

    public HalfgoodRepository() {
        init("half::");
    }

    public THalfgood find(int id) {
        THalfgood halfgood = getCache(id, THalfgood.class);
        if (null != halfgood) {
            return halfgood;
        }

        // 缓存没有就查询数据库
        halfgood = halfgoodMapper.selectByPrimaryKey(id);
        if (null != halfgood) {
            setCache(id, halfgood);
        }
        return halfgood;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            THalfgoodExample example = new THalfgoodExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) halfgoodMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            THalfgoodExample example = new THalfgoodExample();
            example.or().andGidEqualTo(gid);
            total = (int) halfgoodMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<THalfgood> pagination(int gid, int page, int limit, String search) {
        THalfgoodExample example = new THalfgoodExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return halfgoodMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在商品编号
     */
    public boolean checkCode(int gid, String code, int id) {
        THalfgoodExample example = new THalfgoodExample();
        example.or().andGidEqualTo(gid).andCodeEqualTo(code);
        if (0 == id) {
            return null != halfgoodMapper.selectOneByExample(example);
        } else {
            THalfgood halfgood = halfgoodMapper.selectOneByExample(example);
            return null != halfgood && !halfgood.getId().equals(id);
        }
    }

    /*
     * desc: 判断公司是否存在商品名
     */
    public boolean checkName(int gid, String name, int id) {
        THalfgoodExample example = new THalfgoodExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != halfgoodMapper.selectOneByExample(example);
        } else {
            THalfgood halfgood = halfgoodMapper.selectOneByExample(example);
            return null != halfgood && !halfgood.getId().equals(id);
        }
    }

    public boolean insert(THalfgood row) {
        if (halfgoodMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(THalfgood row) {
        if (halfgoodMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        THalfgood halfgood = find(id);
        if (null == halfgood) {
            return false;
        }
        delCache(id);
        delTotalCache(halfgood.getGid());
        return halfgoodMapper.deleteByPrimaryKey(id) > 0;
    }
}
