package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TShInOrderMapper;
import com.cxb.storehelperserver.model.TShInOrder;
import com.cxb.storehelperserver.model.TShInOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品入库订单仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class ShInOrderRepository extends BaseRepository<TShInOrder> {
    @Resource
    private TShInOrderMapper shInOrderMapper;

    public ShInOrderRepository() {
        init("shInOrder::");
    }

    public TShInOrder find(int id) {
        TShInOrder shInOrder = getCache(id, TShInOrder.class);
        if (null != shInOrder) {
            return shInOrder;
        }

        // 缓存没有就查询数据库
        shInOrder = shInOrderMapper.selectByPrimaryKey(id);
        if (null != shInOrder) {
            setCache(id, shInOrder);
        }
        return shInOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TShInOrderExample example = new TShInOrderExample();
        example.or().andGidEqualTo(gid);
        return null != shInOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TShInOrder row) {
        if (shInOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TShInOrder row) {
        if (shInOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return shInOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
