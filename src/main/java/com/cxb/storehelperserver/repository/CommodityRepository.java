package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCommodityMapper;
import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TCommodityExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 商品仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class CommodityRepository extends BaseRepository<TCommodity> {
    @Resource
    private TCommodityMapper commodityMapper;

    public CommodityRepository() {
        init("comm::");
    }

    public TCommodity find(int id) {
        TCommodity commodity = getCache(id, TCommodity.class);
        if (null != commodity) {
            return commodity;
        }

        // 缓存没有就查询数据库
        commodity = commodityMapper.selectByPrimaryKey(id);
        if (null != commodity) {
            setCache(id, commodity);
        }
        return commodity;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TCommodityExample example = new TCommodityExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) commodityMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TCommodityExample example = new TCommodityExample();
            example.or().andGidEqualTo(gid);
            total = (int) commodityMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TCommodity> pagination(int gid, int page, int limit, String search) {
        TCommodityExample example = new TCommodityExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return commodityMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在商品编号
     */
    public boolean checkCode(int gid, String code, int id) {
        TCommodityExample example = new TCommodityExample();
        example.or().andGidEqualTo(gid).andCodeEqualTo(code);
        if (0 == id) {
            return null != commodityMapper.selectOneByExample(example);
        } else {
            TCommodity commodity = commodityMapper.selectOneByExample(example);
            return null != commodity && !commodity.getId().equals(id);
        }
    }

    /*
     * desc: 判断公司是否存在商品名
     */
    public boolean checkName(int gid, String name, int id) {
        TCommodityExample example = new TCommodityExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != commodityMapper.selectOneByExample(example);
        } else {
            TCommodity commodity = commodityMapper.selectOneByExample(example);
            return null != commodity && !commodity.getId().equals(id);
        }
    }

    public boolean insert(TCommodity row) {
        if (commodityMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCommodity row) {
        if (commodityMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return commodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
