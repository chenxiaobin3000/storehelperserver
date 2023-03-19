package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudDayMapper;
import com.cxb.storehelperserver.model.TCloudDay;
import com.cxb.storehelperserver.model.TCloudDayExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudDayMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓库存日快照仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class CloudDayRepository extends BaseRepository<TCloudDay> {
    @Resource
    private TCloudDayMapper cloudDayMapper;

    @Resource
    private MyCloudDayMapper myCloudDayMapper;

    public CloudDayRepository() {
        init("cloudDay::");
    }

    public TCloudDay find(int sid, int id, Date date) {
        TCloudDayExample example = new TCloudDayExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(id).andCdateEqualTo(date);
        return cloudDayMapper.selectOneByExample(example);
    }

    public List<MyStockReport> findReport(int gid, int sid, int ctype, Date start, Date end) {
        return myCloudDayMapper.selectReport(gid, sid, ctype, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, Date date, String search) {
        if (null != search) {
            return myCloudDayMapper.count(gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            TCloudDayExample example = new TCloudDayExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) cloudDayMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myCloudDayMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), "%" + search + "%");
        } else {
            return myCloudDayMapper.pagination((page - 1) * limit, limit, gid, sid, new java.sql.Date(date.getTime()), null);
        }
    }

    public boolean insert(TCloudDay row) {
        return cloudDayMapper.insert(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TCloudDayExample example = new TCloudDayExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return cloudDayMapper.deleteByExample(example) > 0;
    }
}