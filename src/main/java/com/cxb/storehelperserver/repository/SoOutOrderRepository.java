package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoOutOrderMapper;
import com.cxb.storehelperserver.model.TSoOutOrder;
import com.cxb.storehelperserver.model.TSoOutOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料出库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SoOutOrderRepository extends BaseRepository<TSoOutOrder> {
    @Resource
    private TSoOutOrderMapper soOutOrderMapper;

    public SoOutOrderRepository() {
        init("soOutOrder::");
    }

    public TSoOutOrder find(int id) {
        TSoOutOrder soOutOrder = getCache(id, TSoOutOrder.class);
        if (null != soOutOrder) {
            return soOutOrder;
        }

        // 缓存没有就查询数据库
        soOutOrder = soOutOrderMapper.selectByPrimaryKey(id);
        if (null != soOutOrder) {
            setCache(id, soOutOrder);
        }
        return soOutOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TSoOutOrderExample example = new TSoOutOrderExample();
        example.or().andGidEqualTo(gid);
        return null != soOutOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSoOutOrder row) {
        if (soOutOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoOutOrder row) {
        if (soOutOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soOutOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
