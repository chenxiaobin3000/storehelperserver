package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudMonthMapper;
import com.cxb.storehelperserver.model.TCloudMonth;
import com.cxb.storehelperserver.model.TCloudMonthExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudMonthMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓库存月快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class CloudMonthRepository extends BaseRepository<TCloudMonth> {
    @Resource
    private TCloudMonthMapper cloudMonthMapper;

    @Resource
    private MyCloudMonthMapper myCloudMonthMapper;

    public CloudMonthRepository() {
        init("cloudMonth::");
    }

    public TCloudMonth find(int sid, int id, Date date) {
        TCloudMonthExample example = new TCloudMonthExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return cloudMonthMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myCloudMonthMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date date, String search) {
        if (null != search) {
            return myCloudMonthMapper.count(gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TCloudMonthExample example = new TCloudMonthExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) cloudMonthMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myCloudMonthMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myCloudMonthMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TCloudMonth row) {
        return cloudMonthMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TCloudMonthExample example = new TCloudMonthExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return cloudMonthMapper.deleteByExample(example) > 0;
    }
}