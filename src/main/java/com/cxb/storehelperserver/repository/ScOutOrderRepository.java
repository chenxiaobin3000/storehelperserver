package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TScOutOrderMapper;
import com.cxb.storehelperserver.model.TScOutOrder;
import com.cxb.storehelperserver.model.TScOutOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品出库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ScOutOrderRepository extends BaseRepository<TScOutOrder> {
    @Resource
    private TScOutOrderMapper scOutOrderMapper;

    public ScOutOrderRepository() {
        init("scOutOrder::");
    }

    public TScOutOrder find(int id) {
        TScOutOrder scOutOrder = getCache(id, TScOutOrder.class);
        if (null != scOutOrder) {
            return scOutOrder;
        }

        // 缓存没有就查询数据库
        scOutOrder = scOutOrderMapper.selectByPrimaryKey(id);
        if (null != scOutOrder) {
            setCache(id, scOutOrder);
        }
        return scOutOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TScOutOrderExample example = new TScOutOrderExample();
        example.or().andGidEqualTo(gid);
        return null != scOutOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TScOutOrder row) {
        if (scOutOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TScOutOrder row) {
        if (scOutOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return scOutOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
