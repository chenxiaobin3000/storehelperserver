package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TSdOutOrderMapper;
import com.cxb.storehelperserver.model.TSdOutOrder;
import com.cxb.storehelperserver.model.TSdOutOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 废料出库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class SdOutOrderRepository extends BaseRepository<TSdOutOrder> {
    @Resource
    private TSdOutOrderMapper sdOutOrderMapper;

    public SdOutOrderRepository() {
        init("sdOutOrder::");
    }

    public TSdOutOrder find(int id) {
        TSdOutOrder sdOutOrder = getCache(id, TSdOutOrder.class);
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
        TSdOutOrderExample example = new TSdOutOrderExample();
        example.or().andGidEqualTo(gid);
        return null != sdOutOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TSdOutOrder row) {
        if (sdOutOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TSdOutOrder row) {
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
