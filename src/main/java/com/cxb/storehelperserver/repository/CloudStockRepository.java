package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudStockMapper;
import com.cxb.storehelperserver.model.TCloudStock;
import com.cxb.storehelperserver.model.TCloudStockExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudStockMapper;
import com.cxb.storehelperserver.repository.model.MyStockCommodity;
import com.cxb.storehelperserver.repository.model.MyStockReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.CommodityType;

/**
 * desc: 云仓库存明细仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class CloudStockRepository extends BaseRepository<TCloudStock> {
    @Resource
    private TCloudStockMapper cloudStockMapper;

    @Resource
    private MyCloudStockMapper myCloudStockMapper;

    public CloudStockRepository() {
        init("cloudStock::");
    }

    public List<MyStockReport> findReport(int gid, int sid, int ctype, Date start, Date end) {
        return myCloudStockMapper.selectReport(gid, sid, ctype, start, end);
    }

    public List<MyStockCommodity> findHistory(int gid, int sid, int ctype, int cid, Date start, Date end) {
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myCloudStockMapper.selectHistory_commodity(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case HALFGOOD:
                return myCloudStockMapper.selectHistory_halfgood(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case ORIGINAL:
                return myCloudStockMapper.selectHistory_original(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            case STANDARD:
                return myCloudStockMapper.selectHistory_standard(gid, sid, cid, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));
            default:
                return null;
        }
    }

    public int total(int gid, int sid, int ctype, Date start, Date end, String search) {
        if (null != search) {
            switch (CommodityType.valueOf(ctype)) {
                case COMMODITY:
                    return myCloudStockMapper.count_commodity(gid, sid, start, end, "%" + search + "%");
                case HALFGOOD:
                    return myCloudStockMapper.count_halfgood(gid, sid, start, end, "%" + search + "%");
                case ORIGINAL:
                    return myCloudStockMapper.count_original(gid, sid, start, end, "%" + search + "%");
                case STANDARD:
                    return myCloudStockMapper.count_standard(gid, sid, start, end, "%" + search + "%");
                default:
                    return 0;
            }
        } else {
            TCloudStockExample example = new TCloudStockExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(start).andCdateLessThanOrEqualTo(end);
            return (int) cloudStockMapper.countByExample(example);
        }
    }

    public List<MyStockCommodity> pagination(int gid, int sid, int page, int limit, int ctype, Date start, Date end, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myCloudStockMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, start, end, key);
            case HALFGOOD:
                return myCloudStockMapper.pagination_halfgood((page - 1) * limit, limit, gid, sid, start, end, key);
            case ORIGINAL:
                return myCloudStockMapper.pagination_original((page - 1) * limit, limit, gid, sid, start, end, key);
            case STANDARD:
                return myCloudStockMapper.pagination_standard((page - 1) * limit, limit, gid, sid, start, end, key);
            default:
                return null;
        }
    }

    public boolean insert(int gid, int sid, int otype, Integer oid, int ctype, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TCloudStock row = new TCloudStock();
        row.setGid(gid);
        row.setSid(sid);
        row.setOtype(otype);
        row.setOid(null == oid ? 0 : oid);
        row.setCtype(ctype);
        row.setCid(cid);
        row.setPrice(price);
        row.setWeight(weight);
        row.setValue(value);
        row.setCdate(cdate);
        return cloudStockMapper.insert(row) > 0;
    }
}
