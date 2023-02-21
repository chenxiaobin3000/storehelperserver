package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudWeekMapper;
import com.cxb.storehelperserver.model.TCloudWeek;
import com.cxb.storehelperserver.model.TCloudWeekExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudWeekMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓库存周快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class CloudWeekRepository extends BaseRepository<TCloudWeek> {
    @Resource
    private TCloudWeekMapper cloudWeekMapper;

    @Resource
    private MyCloudWeekMapper myCloudWeekMapper;

    public CloudWeekRepository() {
        init("cloudWeek::");
    }

    public TCloudWeek find(int sid, int id, Date date) {
        TCloudWeekExample example = new TCloudWeekExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return cloudWeekMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, Date start, Date end) {
        return myCloudWeekMapper.selectReport(gid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date date, String search) {
        if (null != search) {
            return myCloudWeekMapper.count(gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TCloudWeekExample example = new TCloudWeekExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) cloudWeekMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myCloudWeekMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myCloudWeekMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TCloudWeek row) {
        return cloudWeekMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TCloudWeekExample example = new TCloudWeekExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return cloudWeekMapper.deleteByExample(example) > 0;
    }
}