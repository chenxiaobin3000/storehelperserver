package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudRemarkMapper;
import com.cxb.storehelperserver.model.TCloudRemark;
import com.cxb.storehelperserver.model.TCloudRemarkExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓备注关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CloudRemarkRepository extends BaseRepository<List> {
    @Resource
    private TCloudRemarkMapper cloudRemarkMapper;

    public CloudRemarkRepository() {
        init("cloudRemark::");
    }

    public TCloudRemark find(int id) {
        return cloudRemarkMapper.selectByPrimaryKey(id);
    }

    public List<TCloudRemark> findByOid(int oid) {
        List<TCloudRemark> cloudRemarks = getCache(oid, List.class);
        if (null != cloudRemarks) {
            return cloudRemarks;
        }

        // 缓存没有就查询数据库
        TCloudRemarkExample example = new TCloudRemarkExample();
        example.or().andOidEqualTo(oid);
        cloudRemarks = cloudRemarkMapper.selectByExample(example);
        if (null != cloudRemarks) {
            setCache(oid, cloudRemarks);
        }
        return cloudRemarks;
    }

    public boolean insert(int oid, String remark, Date cdate) {
        TCloudRemark row = new TCloudRemark();
        row.setOid(oid);
        row.setRemark(remark);
        row.setCdate(cdate);
        if (cloudRemarkMapper.insert(row) > 0) {
            delCache(oid);
            return true;
        }
        return false;
    }

    public boolean update(TCloudRemark row) {
        if (cloudRemarkMapper.updateByPrimaryKey(row) > 0) {
            delCache(row.getOid());
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCloudRemark cloudRemark = cloudRemarkMapper.selectByPrimaryKey(id);
        if (null == cloudRemark) {
            return false;
        }
        delCache(cloudRemark.getOid());
        return cloudRemarkMapper.deleteByPrimaryKey(id) > 0;
    }
}
