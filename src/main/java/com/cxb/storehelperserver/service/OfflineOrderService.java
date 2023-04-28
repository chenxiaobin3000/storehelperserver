package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cxb.storehelperserver.util.TypeDefine.ReviewType;

/**
 * desc: 销售订单缓存业务
 * auth: cxb
 * date: 2023/1/27
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OfflineOrderService extends BaseService<HashMap> {
    @Resource
    private OfflineOrderRepository offlineOrderRepository;

    @Resource
    private OfflineCommodityRepository offlineCommodityRepository;

    @Resource
    private OfflineAttachmentRepository offlineAttachmentRepository;

    @Resource
    private OfflineRemarkRepository offlineRemarkRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private DateUtil dateUtil;

    public OfflineOrderService() {
        init("offlineServ::");
    }

    public HashMap<String, Object> find(int oid) {
        HashMap<String, Object> datas = getCache(oid, HashMap.class);
        if (null != datas) {
            return datas;
        }

        // 商品
        SimpleDateFormat dateFormat = dateUtil.getDateFormat();
        val commoditys = new ArrayList<HashMap<String, Object>>();
        val offlineCommodities = offlineCommodityRepository.find(oid);
        if (null != offlineCommodities && !offlineCommodities.isEmpty()) {
            for (TOfflineCommodity sc : offlineCommodities) {
                val data = new HashMap<String, Object>();
                data.put("id", sc.getId());
                data.put("cid", sc.getCid());
                data.put("price", sc.getPrice());
                data.put("value", sc.getValue());
                commoditys.add(data);

                // 获取商品单位信息
                TCommodity find1 = commodityRepository.find(sc.getCid());
                if (null != find1) {
                    data.put("code", find1.getCode());
                    data.put("name", find1.getName());
                }
            }
        }

        // 附件,运费,备注
        datas = new HashMap<>();
        datas.put("comms", commoditys);
        datas.put("attrs", offlineAttachmentRepository.findByOid(oid));

        // 备注
        val remarks = offlineRemarkRepository.findByOid(oid);
        if (null != remarks && !remarks.isEmpty()) {
            val tmps = new ArrayList<HashMap<String, Object>>();
            for (TOfflineRemark remark : remarks) {
                val tmp = new HashMap<String, Object>();
                tmp.put("id", remark.getId());
                tmp.put("remark", remark.getRemark());
                tmp.put("cdate", dateFormat.format(remark.getCdate()));
                tmps.add(tmp);
            }
            datas.put("remarks", tmps);
        }
        setCache(oid, datas);
        return datas;
    }

    public int total(int gid, int type, ReviewType review, String date, String search) {
        if (null == search) {
            return offlineOrderRepository.total(gid, type, review, date);
        } else {
            TCommodity commodity = commodityRepository.search(search);
            if (null == commodity) {
                return 0;
            }
            return offlineCommodityRepository.total(gid, type, review, date, commodity.getId());
        }
    }

    public List<TOfflineOrder> pagination(int gid, int type, int page, int limit, ReviewType review, String date, String search) {
        if (null == search) {
            return offlineOrderRepository.pagination(gid, type, page, limit, review, date);
        } else {
            TCommodity commodity = commodityRepository.search(search);
            if (null == commodity) {
                return null;
            }
            return offlineCommodityRepository.pagination(gid, type, page, limit, review, date, commodity.getId());
        }
    }

    public String update(int oid, List<TOfflineCommodity> comms, List<Integer> attrs) {
        delCache(oid);
        for (TOfflineCommodity c : comms) {
            c.setOid(oid);
        }
        if (!offlineCommodityRepository.update(comms, oid)) {
            return "生成订单商品信息失败";
        }

        // 没有附件直接返回
        if (null == attrs) {
            return null;
        }

        // 删除多余附件
        val offlineAttachments = offlineAttachmentRepository.findByOid(oid);
        if (null != offlineAttachments) {
            for (TOfflineAttachment attr : offlineAttachments) {
                boolean find = false;
                for (Integer aid : attrs) {
                    if (attr.getId().equals(aid)) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    if (!offlineAttachmentRepository.delete(oid, attr.getId())) {
                        return "删除订单附件失败";
                    }
                }
            }
        }

        // 添加新附件
        for (Integer attr : attrs) {
            TOfflineAttachment offlineAttachment = offlineAttachmentRepository.find(attr);
            if (null != offlineAttachment) {
                if (null == offlineAttachment.getOid() || 0 == offlineAttachment.getOid()) {
                    offlineAttachment.setOid(oid);
                    if (!offlineAttachmentRepository.update(offlineAttachment)) {
                        return "添加订单附件失败";
                    }
                }
            }
        }
        return null;
    }

    public void clean(int oid) {
        delCache(oid);
    }
}
