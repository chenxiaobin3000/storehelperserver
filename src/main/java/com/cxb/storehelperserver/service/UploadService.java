package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.model.TScInAttachment;
import com.cxb.storehelperserver.model.TScOutAttachment;
import com.cxb.storehelperserver.model.TSoInAttachment;
import com.cxb.storehelperserver.model.TSoOutAttachment;
import com.cxb.storehelperserver.repository.ScInAttachmentRepository;
import com.cxb.storehelperserver.repository.ScOutAttachmentRepository;
import com.cxb.storehelperserver.repository.SoInAttachmentRepository;
import com.cxb.storehelperserver.repository.SoOutAttachmentRepository;
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
    private SoInAttachmentRepository soInAttachmentRepository;

    @Resource
    private SoOutAttachmentRepository soOutAttachmentRepository;

    @Resource
    private DateUtil dateUtil;

    @Value("${store-app.config.imagesrc}")
    private int imagesrc;

    @Value("${store-app.config.imagepath}")
    private String imagepath;

    public RestResult addAttach(int id, TypeDefine.AttachType type, MultipartFile files[]) {
        SimpleDateFormat simpleDateFormat = dateUtil.getDateFormat();
        val data = new HashMap<String, Integer>();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getOriginalFilename();
            String path = imagepath + simpleDateFormat.format(new Date());
            File dest = new File(path + '/' + name);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                files[i].transferTo(dest);
            } catch (Exception e) {
                return RestResult.fail("上传失败，请再试一次");
            }

            switch (type) {
                case ATTACH_COMM_IN_ORDER:
                    TScInAttachment scInAttachment = new TScInAttachment();
                    scInAttachment.setOrder(0);
                    scInAttachment.setSrc(imagesrc);
                    scInAttachment.setPath(path);
                    scInAttachment.setName(name);
                    if (!scInAttachmentRepository.insert(scInAttachment)) {
                        return RestResult.fail("写入数据失败，请联系管理员");
                    }
                    break;
                case ATTACHMENT_COMM_OUT_ORDER:
                    TScOutAttachment scOutAttachment = new TScOutAttachment();
                    scOutAttachment.setOrder(0);
                    scOutAttachment.setSrc(imagesrc);
                    scOutAttachment.setPath(path);
                    scOutAttachment.setName(name);
                    if (!scOutAttachmentRepository.insert(scOutAttachment)) {
                        return RestResult.fail("写入数据失败，请联系管理员");
                    }
                    break;
                case ATTACHMENT_ORI_IN_ORDER:
                    TSoInAttachment soInAttachment = new TSoInAttachment();
                    soInAttachment.setOrder(0);
                    soInAttachment.setSrc(imagesrc);
                    soInAttachment.setPath(path);
                    soInAttachment.setName(name);
                    if (!soInAttachmentRepository.insert(soInAttachment)) {
                        return RestResult.fail("写入数据失败，请联系管理员");
                    }
                    break;
                default:
                    TSoOutAttachment soOutAttachment = new TSoOutAttachment();
                    soOutAttachment.setOrder(0);
                    soOutAttachment.setSrc(imagesrc);
                    soOutAttachment.setPath(path);
                    soOutAttachment.setName(name);
                    if (!soOutAttachmentRepository.insert(soOutAttachment)) {
                        return RestResult.fail("写入数据失败，请联系管理员");
                    }
                    break;
            }
        }
        return RestResult.ok(data);
    }
}
