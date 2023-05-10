package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineCommodityMapper;
import com.cxb.storehelperserver.model.TOfflineCommodity;
import com.cxb.storehelperserver.model.TOfflineCommodityExample;
import com.cxb.storehelperserver.model.TOfflineOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CompleteType;
import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 线下销售出入库商品仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class OfflineCommodityRepository extends BaseRepository<List> {
    @Resource
    private TOfflineCommodityMapper offlineCommodityMapper;

    public OfflineCommodityRepository() {
        init("offlineComm::");
    }

    public List<TOfflineCommodity> find(int oid) {
        List<TOfflineCommodity> offlineCommoditys = getCache(oid, List.class);
        if (null != offlineCommoditys) {
            return offlineCommoditys;
        }

        // 缓存没有就查询数据库
        TOfflineCommodityExample example = new TOfflineCommodityExample();
        example.or().andOidEqualTo(oid);
        offlineCommoditys = offlineCommodityMapper.selectByExample(example);
        if (null != offlineCommoditys) {
            setCache(oid, offlineCommoditys);
        }
        return offlineCommoditys;
    }

    public int total(int gid, int aid, int type, ReviewType review, CompleteType complete, String search, int cid) {
        return 0;
    }

    public List<TOfflineOrder> pagination(int gid, int aid, int type, int page, int limit, ReviewType review, CompleteType complete, String date, int cid) {
        return null;
    }

    // 注意：数据被缓存在OfflineCommodityService，所以不能直接调用该函数
    public boolean update(List<TOfflineCommodity> rows, int oid) {
        delete(oid);
        for (TOfflineCommodity offlineCommodity : rows) {
            if (offlineCommodityMapper.insert(offlineCommodity) < 0) {
                return false;
            }
        }
        setCache(oid, rows);
        return true;
    }

    public boolean delete(int oid) {
        delCache(oid);
        TOfflineCommodityExample example = new TOfflineCommodityExample();
        example.or().andOidEqualTo(oid);
        return offlineCommodityMapper.deleteByExample(example) > 0;
    }
}
