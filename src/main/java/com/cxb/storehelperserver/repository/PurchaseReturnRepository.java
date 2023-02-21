package com.cxb.storehelperserver.repository;

import com.cxb.storehelperserver.mapper.TPurchaseReturnMapper;
import com.cxb.storehelperserver.model.TPurchaseReturn;
import com.cxb.storehelperserver.model.TPurchaseReturnExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * desc: 采购退货关联仓库
 * auth: cxb
 * date: 2023/1/21
 */
@Slf4j
@Repository
public class PurchaseReturnRepository extends BaseRepository<TPurchaseReturn> {
    @Resource
    private TPurchaseReturnMapper purchaseReturnMapper;

    public PurchaseReturnRepository() {
        init("purRet::");
    }

    public TPurchaseReturn find(int rid) {
        TPurchaseReturn purchaseReturn = getCache(rid, TPurchaseReturn.class);
        if (null != purchaseReturn) {
            return purchaseReturn;
        }

        // 缓存没有就查询数据库
        TPurchaseReturnExample example = new TPurchaseReturnExample();
        example.or().andRidEqualTo(rid);
        purchaseReturn = purchaseReturnMapper.selectOneByExample(example);
        if (null != purchaseReturn) {
            setCache(rid, purchaseReturn);
        }
        return purchaseReturn;
    }

    public List<TPurchaseReturn> findByPid(int pid) {
        TPurchaseReturnExample example = new TPurchaseReturnExample();
        example.or().andPidEqualTo(pid);
        return purchaseReturnMapper.selectByExample(example);
    }

    public boolean checkByPid(int pid) {
        TPurchaseReturnExample example = new TPurchaseReturnExample();
        example.or().andPidEqualTo(pid);
        return null != purchaseReturnMapper.selectOneByExample(example);
    }

    public boolean insert(int pid, int rid) {
        TPurchaseReturn row = new TPurchaseReturn();
        row.setPid(pid);
        row.setRid(rid);
        if (purchaseReturnMapper.insert(row) > 0) {
            setCache(row.getRid(), row);
            return true;
        }
        return false;
    }

    public boolean update(TPurchaseReturn row) {
        if (purchaseReturnMapper.updateByPrimaryKey(row) > 0) {
            setCache(row.getRid(), row);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        TPurchaseReturn purchaseReturn = purchaseReturnMapper.selectByPrimaryKey(id);
        if (null == purchaseReturn) {
            return false;
        }
        delCache(purchaseReturn.getRid());
        return purchaseReturnMapper.deleteByPrimaryKey(id) > 0;
    }
}
