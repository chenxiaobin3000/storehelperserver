package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TCloudMapper;
import com.cxb.storehelperserver.model.TCloud;
import com.cxb.storehelperserver.model.TCloudExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 云仓仓库
 * auth: cxb
 * date: 2023/1/4
 */
@Slf4j
@Repository
public class CloudRepository extends BaseRepository<TCloud> {
    @Resource
    private TCloudMapper cloudMapper;

    public CloudRepository() {
        init("cloud::");
    }

    public TCloud find(int id) {
        TCloud cloud = getCache(id, TCloud.class);
        if (null != cloud) {
            return cloud;
        }

        // 缓存没有就查询数据库
        cloud = cloudMapper.selectByPrimaryKey(id);
        if (null != cloud) {
            setCache(id, cloud);
        }
        return cloud;
    }

    public int total(int gid, String search) {
        // 包含搜索的不缓存
        if (null != search) {
            TCloudExample example = new TCloudExample();
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
            return (int) cloudMapper.countByExample(example);
        } else {
            int total = getTotalCache(gid);
            if (0 != total) {
                return total;
            }
            TCloudExample example = new TCloudExample();
            example.or().andGidEqualTo(gid);
            total = (int) cloudMapper.countByExample(example);
            setTotalCache(gid, total);
            return total;
        }
    }

    public List<TCloud> pagination(int gid, int page, int limit, String search) {
        TCloudExample example = new TCloudExample();
        if (null == search) {
            example.or().andGidEqualTo(gid);
        } else {
            example.or().andGidEqualTo(gid).andNameLike("%" + search + "%");
        }
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        return cloudMapper.selectByExample(example);
    }

    /*
     * desc: 判断公司是否存在仓库
     */
    public boolean check(int gid, String name, int id) {
        TCloudExample example = new TCloudExample();
        example.or().andGidEqualTo(gid).andNameEqualTo(name);
        if (0 == id) {
            return null != cloudMapper.selectOneByExample(example);
        } else {
            TCloud cloud = cloudMapper.selectOneByExample(example);
            return null != cloud && !cloud.getId().equals(id);
        }
    }

    public boolean insert(TCloud row) {
        if (cloudMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TCloud row) {
        if (cloudMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TCloud cloud = find(id);
        if (null == cloud) {
            return false;
        }
        delCache(id);
        delTotalCache(cloud.getGid());
        return cloudMapper.deleteByPrimaryKey(id) > 0;
    }
}
