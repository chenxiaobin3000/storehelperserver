package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TOfflineRemarkMapper;
import com.cxb.storehelperserver.model.TOfflineRemark;
import com.cxb.storehelperserver.model.TOfflineRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 线下销售备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class OfflineRemarkRepository extends BaseRepository<List> {
    @Resource
    private TOfflineRemarkMapper offlineRemarkMapper;

    public OfflineRemarkRepository() {
        init("offlineRemark::");
    }

    public TOfflineRemark find(int id) {
        return offlineRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TOfflineRemark> findByOid(int oid) {
        List<TOfflineRemark> offlineRemarks = getCache(oid, List.class);
        if (null != offlineRemarks) {
            return offlineRemarks;
        }

        // 缓存没有就查询数据库
        TOfflineRemarkExample example = new TOfflineRemarkExample();
        example.or().andOidEqualTo(oid);
        offlineRemarks = offlineRemarkMapper.selectByExample(example);
        if (null != offlineRemarks) {
            setCache(oid, offlineRemarks);
        }
        return offlineRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TOfflineRemark row = new TOfflineRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (offlineRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TOfflineRemark row) {
        if (offlineRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TOfflineRemark offlineRemark = offlineRemarkMapper.selectByPrimaryKey(id);
        if (null == offlineRemark) {
            return false;
        }
        delCache(offlineRemark.getOid());
        return offlineRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
