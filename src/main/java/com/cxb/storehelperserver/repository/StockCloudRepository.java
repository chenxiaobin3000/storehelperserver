package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockCloudMapper;
import com.cxb.storehelperserver.model.TStockCloud;
import com.cxb.storehelperserver.model.TStockCloudExample;
import com.cxb.storehelperserver.repository.mapper.MyStockCloudMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * desc: 云仓库存明细仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockCloudRepository extends BaseRepository<TStockCloud> {
    @Resource
    private TStockCloudMapper stockCloudMapper;

    @Resource
    private MyStockCloudMapper myStockCloudMapper;

    public StockCloudRepository() {
        init("stockCloud::");
    }

    public List<MyStockReport> findReport(int gid, int sid, int aid, int asid, Date start, Date end) {
        return myStockCloudMapper.selectReport(gid, sid, aid, asid, start, end);
    }

    public List<MyStockCommodity> findHistoryAll(int gid, int sid, int aid, int asid, Date start, Date end) {
        return myStockCloudMapper.selectHistory_all(gid, sid, aid, asid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public List<MyStockCommodity> findHistory(int gid, int sid, int aid, int asid, int cid, Date start, Date end) {
        return myStockCloudMapper.selectHistory(gid, sid, aid, asid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
    }

    public int total(int gid, int sid, int aid, int asid, Date start, Date end, String search) {
        if (null != search) {
            return myStockCloudMapper.count(gid, sid, aid, asid, start, end, "%" + search + "%");
        } else {
            TStockCloudExample example = new TStockCloudExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andAidEqualTo(aid).andAsidEqualTo(asid)
                    .andCdateGreaterThanOrEqualTo(start).andCdateLessThanOrEqualTo(end);
            return (int) stockCloudMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int aid, int asid, int page, int limit, Date start, Date end, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        return myStockCloudMapper.pagination((page - 1) * limit, limit, gid, sid, aid, asid, start, end, key);
    }

    public boolean insert(int gid, int sid, int aid, int asid, int otype, Integer oid, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TStockCloud row = new TStockCloud();
        row.setGid(gid);
        row.setSid(sid);
        row.setAid(aid);
        row.setAsid(asid);
        row.setOtype(otype);
        row.setOid(null == oid ? 0 : oid);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        row.setCdate(cdate);
        return stockCloudMapper.insert(row) > 0;
    }
}
