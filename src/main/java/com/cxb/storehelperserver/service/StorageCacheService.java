package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.RestResult;
import com.cxb.storehelperserver.util.TypeDefine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * desc: 仓库商品业务
 * auth: cxb
 * date: 2023/1/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageCacheService extends BaseService<HashMap> {
    @Resource
    private StorageOrderRepository storageOrderRepository;

    @Resource
    private StorageOrderCommodityRepository storageOrderCommodityRepository;

    @Resource
    private StorageOrderAttachmentRepository storageOrderAttachmentRepository;

    @Resource
    private CommodityRepository commodityRepository;

    @Resource
    private HalfgoodRepository halfgoodRepository;

    @Resource
    private OriginalRepository originalRepository;

    @Resource
    private StandardRepository standardRepository;

    public StorageCacheService() {
        init("scserv::");
    }

    public HashMap<String, Object> find(int oid) {
        HashMap<String, Object> datas = getCache(oid, HashMap.class);
        if (null != datas) {
            return datas;
        }

        // 商品
        val commoditys = new ArrayList<HashMap<String, Object>>();
        List<TStorageOrderCommodity> storageOrderCommodities = storageOrderCommodityRepository.find(oid);
        if (null != storageOrderCommodities && !storageOrderCommodities.isEmpty()) {
            for (TStorageOrderCommodity sc : storageOrderCommodities) {
                val data = new HashMap<String, Object>();
                data.put("id", sc.getId());
                data.put("cid", sc.getCid());
                data.put("ctype", sc.getCtype());
                data.put("unit", sc.getUnit());
                data.put("value", sc.getValue());
                data.put("price", sc.getPrice());
                commoditys.add(data);

                // 获取商品单位信息
                TypeDefine.CommodityType type = TypeDefine.CommodityType.valueOf(sc.getCtype());
                int cid = sc.getCid();
                switch (type) {
                    case COMMODITY:
                        TCommodity find1 = commodityRepository.find(cid);
                        if (null != find1) {
                            data.put("code", find1.getCode());
                            data.put("name", find1.getName());
                        }
                        break;
                    case HALFGOOD:
                        THalfgood find2 = halfgoodRepository.find(cid);
                        if (null != find2) {
                            data.put("code", find2.getCode());
                            data.put("name", find2.getName());
                        }
                        break;
                    case ORIGINAL:
                        TOriginal find3 = originalRepository.find(cid);
                        if (null != find3) {
                            data.put("code", find3.getCode());
                            data.put("name", find3.getName());
                        }
                        break;
                    case STANDARD:
                        TStandard find4 = standardRepository.find(cid);
                        if (null != find4) {
                            data.put("code", find4.getCode());
                            data.put("name", find4.getName());
                        }
                        break;
                }
            }
        }

        // 附件
        datas = new HashMap<>();
        datas.put("comms", commoditys);
        datas.put("attrs", storageOrderAttachmentRepository.findByOid(oid));
        setCache(oid, datas);
        return datas;
    }

    public String update(int oid, List<TStorageOrderCommodity> comms, List<Integer> attrs) {
        delCache(oid);
        if (!storageOrderCommodityRepository.update(comms, oid)) {
            return "生成订单商品数据失败";
        }

        // 修改附件oid
        for (Integer attr : attrs) {
            TStorageOrderAttachment storageOrderAttachment = storageOrderAttachmentRepository.find(attr);
            if (null != storageOrderAttachment) {
                storageOrderAttachment.setOid(oid);
                if (!storageOrderAttachmentRepository.update(storageOrderAttachment)) {
                    return "添加订单附件失败";
                }
            }
        }
        return null;
    }
}
