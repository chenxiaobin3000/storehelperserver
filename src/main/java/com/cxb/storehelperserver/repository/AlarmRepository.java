package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAlarmMapper;
import com.cxb.storehelperserver.model.TAlarm;
import com.cxb.storehelperserver.model.TAlarmExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 预警仓库
 * auth: cxb
 * date: 2023/2/6
 */
@Slf4j
@Repository
public class AlarmRepository extends BaseRepository<TAlarm> {
    @Resource
    private TAlarmMapper alarmMapper;

    public AlarmRepository() {
        init("alarm::");
    }

    public TAlarm find(int id) {
        TAlarm alarm = getCache(id, TAlarm.class);
        if (null != alarm) {
            return alarm;
        }

        // 缓存没有就查询数据库
        alarm = alarmMapper.selectByPrimaryKey(id);
        if (null != alarm) {
            setCache(id, alarm);
        }
        return alarm;
    }

    public int total(int gid) {
        int total = getTotalCache(gid);
        if (0 != total) {
            return total;
        }
        TAlarmExample example = new TAlarmExample();
        example.or().andGidEqualTo(gid);
        total = (int) alarmMapper.countByExample(example);
        setTotalCache(gid, total);
        return total;
    }

    public List<TAlarm> pagination(int gid, int page, int limit) {
        TAlarmExample example = new TAlarmExample();
        example.or().andGidEqualTo(gid);
        example.setOffset((page - 1) * limit);
        example.setLimit(limit);
        example.setOrderByClause("ctime desc");
        return alarmMapper.selectByExample(example);
    }

    public boolean insert(TAlarm row) {
        if (alarmMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            delTotalCache(row.getGid());
            return true;
        }
        return false;
    }

    public boolean update(TAlarm row) {
        if (alarmMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TAlarm alarm = find(id);
        if (null == alarm) {
            return false;
        }
        delCache(id);
        delTotalCache(alarm.getGid());
        return alarmMapper.deleteByPrimaryKey(id) > 0;
    }
}
