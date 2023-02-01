package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStockDestroyMapper;
import com.cxb.storehelperserver.model.TStockDestroy;
import com.cxb.storehelperserver.model.TStockDestroyExample;
import com.cxb.storehelperserver.repository.mapper.MyStockDestroyMapper;
import com.cxb.storehelperserver.repository.model.MyStockDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * desc: 仓储废料仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StockDestroyRepository extends BaseRepository<TStockDestroy> {
    @Resource
    private TStockDestroyMapper stockDestroyMapper;

    @Resource
    private MyStockDestroyMapper myStockDestroyMapper;

    public StockDestroyRepository() {
        init("stockD::");
    }

    public TStockDestroy find(int sid, int id, Date date) {
        TStockDestroyExample example = new TStockDestroyExample();
        example.or().andSidEqualTo(sid).andDidEqualTo(id).andCdateEqualTo(date);
        return stockDestroyMapper.selectOneByExample(example);
    }

    public TStockDestroy findLast(int sid, int did) {
        TStockDestroyExample example = new TStockDestroyExample();
        if (0 == did) {
            example.or().andSidEqualTo(sid);
        } else {
            example.or().andSidEqualTo(sid).andDidEqualTo(did);
        }
        example.setOrderByClause("cdate desc");
        return stockDestroyMapper.selectOneByExample(example);
    }

    public int total(int sid, Date date, String search) {
        if (null != search) {
            return myStockDestroyMapper.countByExample(sid, date, "%" + search + "%");
        } else {
            TStockDestroyExample example = new TStockDestroyExample();
            example.or().andSidEqualTo(sid).andCdateEqualTo(date);
            return (int) stockDestroyMapper.countByExample(example);
        }
    }

    public List<MyStockDestroy> pagination(int sid, int page, int limit, Date date, String search) {
        if (null != search) {
            return myStockDestroyMapper.selectByExample((page - 1) * limit, limit, sid, date, "%" + search + "%");
        } else {
            return myStockDestroyMapper.selectByExample((page - 1) * limit, limit, sid, date, null);
        }
    }

    public boolean insert(TStockDestroy row) {
        return stockDestroyMapper.insert(row) > 0;
    }

    public boolean update(TStockDestroy row) {
        return stockDestroyMapper.updateByPrimaryKey(row) > 0;
    }

    public boolean delete(int sid, Date date) {
        TStockDestroyExample example = new TStockDestroyExample();
        example.or().andSidEqualTo(sid).andCdateGreaterThanOrEqualTo(date);
        return stockDestroyMapper.deleteByExample(example) > 0;
    }
}
