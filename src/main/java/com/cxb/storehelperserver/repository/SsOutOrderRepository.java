package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSsOutOrderMapper;
import com.cxb.storehelperserver.model.TSsOutOrder;
import com.cxb.storehelperserver.model.TSsOutOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 标品出库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SsOutOrderRepository extends BaseRepository<TSsOutOrder> {
    @Resource
    private TSsOutOrderMapper sdOutOrderMapper;

    public SsOutOrderRepository() {
        init("sdOutOrder::");
    }

    public TSsOutOrder find(int id) {
        TSsOutOrder sdOutOrder = getCache(id, TSsOutOrder.class);
        if (null != sdOutOrder) {
            return sdOutOrder;
        }

        // 缓存没有就查询数据库
        sdOutOrder = sdOutOrderMapper.selectByPrimaryKey(id);
        if (null != sdOutOrder) {
            setCache(id, sdOutOrder);
        }
        return sdOutOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TSsOutOrderExample example = new TSsOutOrderExample();
        example.or().andGidEqualTo(gid);
        return null != sdOutOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSsOutOrder row) {
        if (sdOutOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSsOutOrder row) {
        if (sdOutOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return sdOutOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
