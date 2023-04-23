package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStorageCommodityMapper;
import com.cxb.storehelperserver.model.TStorageCommodity;
import com.cxb.storehelperserver.model.TStorageCommodityExample;
import com.cxb.storehelperserver.model.TStorageOrder;
import com.cxb.storehelperserver.repository.mapper.MyCommodityCountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 进货出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class StorageCommodityRepository extends BaseRepository<List> {
    @Resource
    private TStorageCommodityMapper storageCommodityMapper;

    @Resource
    private MyCommodityCountMapper myCommodityCountMapper;

    public StorageCommodityRepository() {
        init("storageComm::");
    }

    public TStorageCommodity findOne(int oid, int ctype, int cid) {
        List<TStorageCommodity> storageCommoditys = getCache(oid, List.class);
        if (null != storageCommoditys) {
            for (TStorageCommodity c : storageCommoditys) {
                if (c.getCtype() == ctype && c.getCid() == cid) {
                    return c;
                }
            }
        }

        // 缓存没有就查询数据库
        TStorageCommodityExample example = new TStorageCommodityExample();
        example.or().andOidEqualTo(oid).andCtypeEqualTo(ctype).andCidEqualTo(cid);
        return storageCommodityMapper.selectOneByExample(example);
    }

    public List<TStorageCommodity> find(int oid) {
        List<TStorageCommodity> storageCommoditys = getCache(oid, List.class);
        if (null != storageCommoditys) {
            return storageCommoditys;
        }

        // 缓存没有就查询数据库
        TStorageCommodityExample example = new TStorageCommodityExample();
        example.or().andOidEqualTo(oid);
        storageCommoditys = storageCommodityMapper.selectByExample(example);
        if (null != storageCommoditys) {
            setCache(oid, storageCommoditys);
        }
        return storageCommoditys;
    }

    public BigDecimal count(int oid) {
        return myCommodityCountMapper.count_storage(oid);
    }

    public int total(int gid, int aid, int asid, int type, ReviewType review, CompleteType complete, String search, int cid) {
        return 0;
    }

    public List<TStorageOrder> pagination(int gid, int aid, int asid, int type, int page, int limit, ReviewType review, CompleteType complete, String date, int cid) {
        return null;
    }

    // 注意：数据被缓存在StorageCommodityService，所以不能直接调用该函数
    public boolean update(List<TStorageCommodity> rows, int oid) {
        delete(oid);
        for (TStorageCommodity storageCommodity : rows) {
            if (storageCommodityMapper.insert(storageCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TStorageCommodityExample example = new TStorageCommodityExample();
        example.or().andOidEqualTo(oid);
        return storageCommodityMapper.deleteByExample(example) > 0;
    }
}
