package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TAgreementOrderMapper;
import com.cxb.storehelperserver.model.TAgreementOrder;
import com.cxb.storehelperserver.model.TAgreementOrderExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * desc: 履约出入库订单仓库
 * auth: cxb
 * date: 2023/1/10
 */
@Slf4j
@Repository
public class AgreementOrderRepository extends BaseRepository<TAgreementOrder> {
    @Resource
    private TAgreementOrderMapper agreementOrderMapper;

    public AgreementOrderRepository() {
        init("aOrder::");
    }

    public TAgreementOrder find(int id) {
        TAgreementOrder agreementOrder = getCache(id, TAgreementOrder.class);
        if (null != agreementOrder) {
            return agreementOrder;
        }

        // 缓存没有就查询数据库
        agreementOrder = agreementOrderMapper.selectByPrimaryKey(id);
        if (null != agreementOrder) {
            setCache(id, agreementOrder);
        }
        return agreementOrder;
    }

    /*
     * desc: 判断仓库是否存在属性
     */
    public boolean check(int gid) {
        TAgreementOrderExample example = new TAgreementOrderExample();
        example.or().andGidEqualTo(gid);
        return null != agreementOrderMapper.selectOneByExample(example);
    }

    public boolean insert(TAgreementOrder row) {
        if (agreementOrderMapper.insert(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean update(TAgreementOrder row) {
        if (agreementOrderMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getId(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        delCache(id);
        return agreementOrderMapper.deleteByPrimaryKey(id) > 0;
    }
}
