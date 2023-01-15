package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TShOutOrderMapper;
import com.cxb.storehelperserver.model.TShOutOrder;
import com.cxb.storehelperserver.model.TShOutOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 半成品出库订单仓库
 * auth: cxb
 * date: 2023/1/16
 */
@Slf4j
@Repository
public class ShOutOrderRepository extends BaseRepository<TShOutOrder> {
    @Resource
    private TShOutOrderMapper shOutOrderMapper;

    public ShOutOrderRepository() {
        init("shOutOrder::");
    }

    public TShOutOrder find(int id) {
        TShOutOrder shOutOrder = getCache(id, TShOutOrder.class);
        if (null != shOutOrder) {
            return shOutOrder;
        }

        // 缓存没有就查询数据库
        shOutOrder = shOutOrderMapper.selectByPrimaryKey(id);
        if (null != shOutOrder) {
            setCache(id, shOutOrder);
        }
        return shOutOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TShOutOrderExample example = new TShOutOrderExample();
        example.or().andGidEqualTo(gid);
        return null != shOutOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TShOutOrder row) {
        if (shOutOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TShOutOrder row) {
        if (shOutOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return shOutOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
