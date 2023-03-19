package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudDetailMapper;
import com.cxb.storehelperserver.model.TCloudDetail;
import com.cxb.storehelperserver.model.TCloudDetailExample;
import com.cxb.storehelperserver.repository.mapper.MyCloudDetailMapper;
import com.cxb.storehelperserver.repository.model.MyStockDetail;
import com.cxb.storehelperserver.util.TypeDefine;
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
public class CloudDetailRepository extends BaseRepository<TCloudDetail> {
    @Resource
    private TCloudDetailMapper cloudDetailMapper;

    @Resource
    private MyCloudDetailMapper myCloudDetailMapper;

    public CloudDetailRepository() {
        init("cloudDetail::");
    }

    public TCloudDetail find(int sid, int cid) {
        TCloudDetail stock = getCache(joinKey(sid, cid), TCloudDetail.class);
        if (null != stock) {
            return stock;
        }

        // 缓存没有就查询数据库
        TCloudDetailExample example = new TCloudDetailExample();
        example.or().andSidEqualTo(sid).andCidEqualTo(cid);
        stock = cloudDetailMapper.selectOneByExample(example);
        if (null != stock) {
            setCache(joinKey(sid, cid), stock);
        }
        return stock;
    }

    public int total(int gid, int sid, int ctype, String search) {
        if (null != search) {
            switch (TypeDefine.CommodityType.valueOf(ctype)) {
                case COMMODITY:
                    return myCloudDetailMapper.count_commodity(gid, sid, "%" + search + "%");
                case HALFGOOD:
                    return myCloudDetailMapper.count_halfgood(gid, sid, "%" + search + "%");
                case ORIGINAL:
                    return myCloudDetailMapper.count_original(gid, sid, "%" + search + "%");
                case STANDARD:
                    return myCloudDetailMapper.count_standard(gid, sid, "%" + search + "%");
                default:
                    return 0;
            }
        } else {
            int total = getTotalCache(joinKey(gid, sid));
            if (0 != total) {
                return total;
            }
            TCloudDetailExample example = new TCloudDetailExample();
            example.or().andGidEqualTo(gid).andSidEqualTo(sid);
            total = (int) cloudDetailMapper.countByExample(example);
            setTotalCache(joinKey(gid, sid), total);
            return total;
        }
    }

    public List<MyStockDetail> pagination(int gid, int sid, int page, int limit, int ctype, String search) {
        String key = null;
        if (null != search) {
            key = "%" + search + "%";
        }
        switch (TypeDefine.CommodityType.valueOf(ctype)) {
            case COMMODITY:
                return myCloudDetailMapper.pagination_commodity((page - 1) * limit, limit, gid, sid, key);
            case HALFGOOD:
                return myCloudDetailMapper.pagination_halfgood((page - 1) * limit, limit, gid, sid, key);
            case ORIGINAL:
                return myCloudDetailMapper.pagination_original((page - 1) * limit, limit, gid, sid, key);
            case STANDARD:
                return myCloudDetailMapper.pagination_standard((page - 1) * limit, limit, gid, sid, key);
            default:
                return null;
        }
    }

    public boolean insert(int gid, int sid, int otype, Integer oid, int ctype, int cid, BigDecimal price, int weight, int value, Date cdate) {
        TCloudDetail row = new TCloudDetail();
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
        if (cloudDetailMapper.insert(row) > 0) {
            setCache(joinKey(row.getSid(), row.getCid()), row);
            delTotalCache(joinKey(row.getGid(), row.getSid()));
            return true;
        }
        return false;
    }

    public boolean delete(int sid, int cid) {
        TCloudDetail stock = find(sid, cid);
        if (null == stock) {
            return false;
        }
        delCache(joinKey(stock.getSid(), stock.getCid()));
        delTotalCache(joinKey(stock.getGid(), stock.getSid()));
        return cloudDetailMapper.deleteByPrimaryKey(stock.getId()) > 0;
    }
}
