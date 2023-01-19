package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.*;
import com.cxb.storehelperserver.repository.*;
import com.cxb.storehelperserver.util.DateUtil;
import com.cxb.storehelperserver.util.TypeDefine;
import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * desc: 上传业务
 * auth: cxb
 * date: 2023/1/9
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UploadService {
    @Resource
    private ScInAttachmentRepository scInAttachmentRepository;
    @Resource
    private ScOutAttachmentRepository scOutAttachmentRepository;

    @Resource
    private SdInAttachmentRepository sdInAttachmentRepository;
    @Resource
    private SdOutAttachmentRepository sdOutAttachmentRepository;

    @Resource
    private ShInAttachmentRepository shInAttachmentRepository;
    @Resource
    private ShOutAttachmentRepository shOutAttachmentRepository;

    @Resource
    private SoInAttachmentRepository soInAttachmentRepository;
    @Resource
    private SoOutAttachmentRepository soOutAttachmentRepository;

    @Resource
    private SsInAttachmentRepository ssInAttachmentRepository;
    @Resource
    private SsOutAttachmentRepository ssOutAttachmentRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.imagesrc}")
    private int imagesrc;

    @Value("${store-app.config.imagepath}")
    private String imagepath;

    public RestResult addAttach(int id, TypeDefine.OrderType type, MultipartFile file) {
        if (file.isEmpty()) {
            return RestResult.fail("上传文件失败，请再试一次");
        }
        SimpleDateFormat simpleDateFormat = dateUtil.getSimpleDateFormat();
        val data = new HashMap<String, Integer>();
        String name = file.getOriginalFilename();
        String path = imagepath + simpleDateFormat.format(new Date());
        File dest = new File(path + '/' + name);
        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return RestResult.fail("服务器文件系统异常，请再试一次");
            }
        }
        try {
            log.info(dest.getAbsolutePath());
            file.transferTo(dest);
        } catch (Exception e) {
            log.info(e.toString());
            return RestResult.fail("上传文件异常，请再试一次");
        }

        // 写入数据库
        switch (type) {
            case PRODUCT_COMMODITY_IN_ORDER:
            case AGREEMENT_COMMODITY_IN_ORDER:
                TScInAttachment scInAttachment = new TScInAttachment();
                scInAttachment.setOrid(0);
                scInAttachment.setSrc(imagesrc);
                scInAttachment.setPath(path);
                scInAttachment.setName(name);
                if (!scInAttachmentRepository.insert(scInAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", scInAttachment.getId());
                break;
            case PRODUCT_HALFGOOD_IN_ORDER:
                TShInAttachment shInAttachment = new TShInAttachment();
                shInAttachment.setOrid(0);
                shInAttachment.setSrc(imagesrc);
                shInAttachment.setPath(path);
                shInAttachment.setName(name);
                if (!shInAttachmentRepository.insert(shInAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", shInAttachment.getId());
                break;
            case STORAGE_ORIGINAL_IN_ORDER:
            case PRODUCT_ORIGINAL_IN_ORDER:
                TSoInAttachment soInAttachment = new TSoInAttachment();
                soInAttachment.setOrid(0);
                soInAttachment.setSrc(imagesrc);
                soInAttachment.setPath(path);
                soInAttachment.setName(name);
                if (!soInAttachmentRepository.insert(soInAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", soInAttachment.getId());
                break;
            case STORAGE_STANDARD_IN_ORDER:
            case AGREEMENT_STANDARD_IN_ORDER:
                TSsInAttachment ssInAttachment = new TSsInAttachment();
                ssInAttachment.setOrid(0);
                ssInAttachment.setSrc(imagesrc);
                ssInAttachment.setPath(path);
                ssInAttachment.setName(name);
                if (!ssInAttachmentRepository.insert(ssInAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", ssInAttachment.getId());
                break;
            case AGREEMENT_COMMODITY_OUT_ORDER:
                TScOutAttachment scOutAttachment = new TScOutAttachment();
                scOutAttachment.setOrid(0);
                scOutAttachment.setSrc(imagesrc);
                scOutAttachment.setPath(path);
                scOutAttachment.setName(name);
                if (!scOutAttachmentRepository.insert(scOutAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", scOutAttachment.getId());
                break;
            case PRODUCT_ORIGINAL_OUT_ORDER:
                TSoOutAttachment soOutAttachment = new TSoOutAttachment();
                soOutAttachment.setOrid(0);
                soOutAttachment.setSrc(imagesrc);
                soOutAttachment.setPath(path);
                soOutAttachment.setName(name);
                if (!soOutAttachmentRepository.insert(soOutAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", soOutAttachment.getId());
                break;
            case AGREEMENT_STANDARD_OUT_ORDER:
                TSsOutAttachment ssOutAttachment = new TSsOutAttachment();
                ssOutAttachment.setOrid(0);
                ssOutAttachment.setSrc(imagesrc);
                ssOutAttachment.setPath(path);
                ssOutAttachment.setName(name);
                if (!ssOutAttachmentRepository.insert(ssOutAttachment)) {
                    return RestResult.fail("写入数据失败，请联系管理员");
                }
                data.put("id", ssOutAttachment.getId());
                break;
            default:
                break;
        }
        return RestResult.ok(data);
    }
}
