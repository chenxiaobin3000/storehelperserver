package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageCommodityMapper;
import com.cxb.storehelperserver.model.TStorageCommodity;
import com.cxb.storehelperserver.model.TStorageCommodityExample;
import com.cxb.storehelperserver.repository.mapper.MyStorageCommodityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 仓储商品仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StorageCommodityRepository {
    @Resource
    private TStorageCommodityMapper storageCommodityMapper;

    @Resource
    private MyStorageCommodityMapper myStorageCommodityMapper;

    public TStorageCommodity find(int sid, int id) {
        TStorageCommodityExample example = new TStorageCommodityExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id);
        return storageCommodityMapper.selectOneByExample(example);
    }

    public int total(int sid, String search) {
        if (null != search) {
            return myStorageCommodityMapper.countByExample(sid, "%" + search + "%");
        } else {
            TStorageCommodityExample example = new TStorageCommodityExample();
            example.or().andSidEqualTo(sid);
            return (int) storageCommodityMapper.countByExample(example);
        }
    }

    public List<TStorageCommodity> pagination(int sid, int page, int limit, String search) {
        if (null != search) {
            return myStorageCommodityMapper.selectByExample((page - 1) * limit, limit, sid, "%" + search + "%");
        } else {
            return myStorageCommodityMapper.selectByExample((page - 1) * limit, limit, sid, null);
        }
    }

    public boolean insert(TStorageCommodity row) {
        return storageCommodityMapper.insert(row) > 0;
    }

    public boolean update(TStorageCommodity row) {
        return storageCommodityMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int id) {
        return storageCommodityMapper.deleteByPrimaryKey(id) > 0;
    }
}
