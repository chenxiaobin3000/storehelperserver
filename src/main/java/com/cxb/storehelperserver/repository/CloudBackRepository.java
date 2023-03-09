package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudBackMapper;
import com.cxb.storehelperserver.model.TCloudBack;
import com.cxb.storehelperserver.model.TCloudBackExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 云仓退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class CloudBackRepository extends BaseRepository<TCloudBack> {
    @Resource
    private TCloudBackMapper cloudBackMapper;

    public CloudBackRepository() {
        init("cloudBack::");
    }

    public TCloudBack find(int oid) {
        TCloudBack cloudBack = getCache(oid, TCloudBack.class);
        if (null != cloudBack) {
            return cloudBack;
        }

        // 缓存没有就查询数据库
        TCloudBackExample example = new TCloudBackExample();
        example.or().andOidEqualTo(oid);
        cloudBack = cloudBackMapper.selectOneByExample(example);
        if (null != cloudBack) {
            setCache(oid, cloudBack);
        }
        return cloudBack;
    }

    public List<TCloudBack> findByAid(int aid) {
        TCloudBackExample example = new TCloudBackExample();
        example.or().andAidEqualTo(aid);
        return cloudBackMapper.selectByExample(example);
    }

    public boolean checkByAid(int aid) {
        TCloudBackExample example = new TCloudBackExample();
        example.or().andAidEqualTo(aid);
        return null != cloudBackMapper.selectOneByExample(example);
    }

    public boolean insert(int oid, int aid) {
        TCloudBack row = new TCloudBack();
        row.setOid(oid);
        row.setAid(aid);
        if (cloudBackMapper.insert(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TCloudBack row) {
        if (cloudBackMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getOid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCloudBack cloudBack = cloudBackMapper.selectByPrimaryKey(id);
        if (null == cloudBack) {
            return false;
        }
        delCache(cloudBack.getOid());
        return cloudBackMapper.deleteByPrimaryKey(id) > 0;
    }
}
