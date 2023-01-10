package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TScInOrderMapper;
import com.cxb.storehelperserver.model.TScInOrder;
import com.cxb.storehelperserver.model.TScInOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 商品入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class ScInOrderRepository extends BaseRepository<TScInOrder> {
    @Resource
    private TScInOrderMapper scInOrderMapper;

    public ScInOrderRepository() {
        init("scInOrder::");
    }

    public TScInOrder find(int id) {
        TScInOrder scInOrder = getCache(id, TScInOrder.class);
        if (null != scInOrder) {
            return scInOrder;
        }

        // 缓存没有就查询数据库
        scInOrder = scInOrderMapper.selectByPrimaryKey(id);
        if (null != scInOrder) {
            setCache(id, scInOrder);
        }
        return scInOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TScInOrderExample example = new TScInOrderExample();
        example.or().andGidEqualTo(gid);
        return null != scInOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TScInOrder row) {
        if (scInOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TScInOrder row) {
        if (scInOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return scInOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
