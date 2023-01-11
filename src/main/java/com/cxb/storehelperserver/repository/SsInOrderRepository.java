package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSsInOrderMapper;
import com.cxb.storehelperserver.model.TSsInOrder;
import com.cxb.storehelperserver.model.TSsInOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SsInOrderRepository extends BaseRepository<TSsInOrder> {
    @Resource
    private TSsInOrderMapper sdInOrderMapper;

    public SsInOrderRepository() {
        init("sdInOrder::");
    }

    public TSsInOrder find(int id) {
        TSsInOrder sdInOrder = getCache(id, TSsInOrder.class);
        if (null != sdInOrder) {
            return sdInOrder;
        }

        // 缓存没有就查询数据库
        sdInOrder = sdInOrderMapper.selectByPrimaryKey(id);
        if (null != sdInOrder) {
            setCache(id, sdInOrder);
        }
        return sdInOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TSsInOrderExample example = new TSsInOrderExample();
        example.or().andGidEqualTo(gid);
        return null != sdInOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSsInOrder row) {
        if (sdInOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSsInOrder row) {
        if (sdInOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return sdInOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
