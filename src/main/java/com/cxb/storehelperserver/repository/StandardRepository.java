package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TStandardMapper;
import com.cxb.storehelperserver.model.TStandard;
import com.cxb.storehelperserver.model.TStandardExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 标品仓库
 * auth: cxb
 * date: 2023/1/13
 */
@Slf4j
@Repository
public class StandardRepository extends BaseRepository<TStandard> {
    @Resource
    private TStandardMapper standardMapper;

    public StandardRepository() {
        init("stan::");
    }

    public TStandard find(int id) {
        TStandard standard = getCache(id, TStandard.class);
        if (null != standard) {
            return standard;
        }

        // 缓存没有就查询数据库
        standard = standardMapper.selectByPrimaryKey(id);
        if (null != standard) {
            setCache(id, standard);
        }
        return standard;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TStandardExample example = new TStandardExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) standardMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TStandardExample example = new TStandardExample();
            example.or().andGidEqualTo(gid);
            total = (int) standardMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TStandard> pagination(int gid, int page, int limit, String search) {
        TStandardExample example = new TStandardExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return standardMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在商品编号
     */
    public boolean checkCode(int gid, String code, int id) {
        TStandardExample example = new TStandardExample();
        example.or().andGidEqualTo(gid).andCodeEqualTo(code);
        if (0 == id) {
            return null != standardMapper.selectOneByExample(example);
        } else {
            TStandard standard = standardMapper.selectOneByExample(example);
            return null != standard && !standard.getId().equals(id);
        }
    }

    /*
     * desc: 判断公司是否存在商品名
     */
    public boolean checkName(int gid, String name, int id) {
        TStandardExample example = new TStandardExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != standardMapper.selectOneByExample(example);
        } else {
            TStandard standard = standardMapper.selectOneByExample(example);
            return null != standard && !standard.getId().equals(id);
        }
    }

    public boolean insert(TStandard row) {
        if (standardMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TStandard row) {
        if (standardMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TStandard standard = find(id);
        if (null == standard) {
            return false;
        }
        delCache(id);
        delTotalCache(standard.getGid());
        return standardMapper.deleteByPrimaryKey(id) > 0;
    }
}
