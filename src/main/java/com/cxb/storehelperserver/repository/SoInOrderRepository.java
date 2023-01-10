package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSoInOrderMapper;
import com.cxb.storehelperserver.model.TSoInOrder;
import com.cxb.storehelperserver.model.TSoInOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 原料入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SoInOrderRepository extends BaseRepository<TSoInOrder> {
    @Resource
    private TSoInOrderMapper soInOrderMapper;

    public SoInOrderRepository() {
        init("soInOrder::");
    }

    public TSoInOrder find(int id) {
        TSoInOrder soInOrder = getCache(id, TSoInOrder.class);
        if (null != soInOrder) {
            return soInOrder;
        }

        // 缓存没有就查询数据库
        soInOrder = soInOrderMapper.selectByPrimaryKey(id);
        if (null != soInOrder) {
            setCache(id, soInOrder);
        }
        return soInOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TSoInOrderExample example = new TSoInOrderExample();
        example.or().andGidEqualTo(gid);
        return null != soInOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSoInOrder row) {
        if (soInOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSoInOrder row) {
        if (soInOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return soInOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
