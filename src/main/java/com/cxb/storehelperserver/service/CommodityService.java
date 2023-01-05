package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TCommodity;
import com.cxb.storehelperserver.model.TUserGroup;
import com.cxb.storehelperserver.repository.CommodityAttrRepository;
import com.cxb.storehelperserver.repository.CommodityRepository;
import com.cxb.storehelperserver.repository.UserGroupRepository;
import com.cxb.storehelperserver.service.CheckService;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * desc: 商品业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CommodityService {
    @Resource
    private CheckService checkService;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private CommodityAttrRepository commodityAttrRepository;

    @Resource
    private UserGroupRepository userGroupRepository;

    public RestResult addCommodity(int id, TCommodity commodity) {
        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 商品名重名检测
        if (commodityRepository.check(commodity.getGid(), commodity.getName())) {
            return RestResult.fail("商品名称已存在");
        }

        if (!commodityRepository.insert(commodity)) {
            return RestResult.fail("添加商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult setCommodity(int id, TCommodity commodity) {
        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        // 商品名重名检测
        if (commodityRepository.check(commodity.getGid(), commodity.getName())) {
            return RestResult.fail("商品名称已存在");
        }

        if (!commodityRepository.update(commodity)) {
            return RestResult.fail("修改商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult delCommodity(int id, int cid) {
        TCommodity commodity = commodityRepository.find(cid);
        if (null == commodity) {
            return RestResult.fail("要删除的商品不存在");
        }

        // 验证公司
        String msg = checkService.checkGroup(id, commodity.getGid());
        if (null != msg) {
            return RestResult.fail(msg);
        }

        if (!commodityRepository.delete(cid)) {
            return RestResult.fail("删除商品信息失败");
        }
        return RestResult.ok();
    }

    public RestResult getGroupCommodity(int id) {
        // 获取公司信息
        TUserGroup group = userGroupRepository.find(id);
        if (null == group) {
            return RestResult.fail("获取公司信息异常");
        }

        List<TCommodity> categories = commodityRepository.findByGroup(group.getGid());
        if (null == categories) {
            return RestResult.fail("获取商品信息异常");
        }

        val data = new HashMap<String, Object>();
        data.put("list", categories);
        return RestResult.ok(data);
    }
}
