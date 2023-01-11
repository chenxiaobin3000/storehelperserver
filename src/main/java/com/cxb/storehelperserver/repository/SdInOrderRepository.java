package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSdInOrderMapper;
import com.cxb.storehelperserver.model.TSdInOrder;
import com.cxb.storehelperserver.model.TSdInOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SdInOrderRepository extends BaseRepository<TSdInOrder> {
    @Resource
    private TSdInOrderMapper sdInOrderMapper;

    public SdInOrderRepository() {
        init("sdInOrder::");
    }

    public TSdInOrder find(int id) {
        TSdInOrder sdInOrder = getCache(id, TSdInOrder.class);
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
        TSdInOrderExample example = new TSdInOrderExample();
        example.or().andGidEqualTo(gid);
        return null != sdInOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSdInOrder row) {
        if (sdInOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSdInOrder row) {
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
