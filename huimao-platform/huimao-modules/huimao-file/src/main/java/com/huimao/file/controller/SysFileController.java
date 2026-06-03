package com.huimao.file.controller;

import com.huimao.file.controller.service.ISysFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.huimao.common.core.domain.R;
import com.huimao.common.core.utils.file.FileUtils;
import com.huimao.system.api.domain.SysFile;

/**
 * 文件请求处理
 *
 * @author huimao
 */
@RestController
public class SysFileController
{
    private static final Logger log = LoggerFactory.getLogger(SysFileController.class);

    @Autowired
    private ISysFileService sysFileService;

    /**
     * 文件上传请求
     */
    @PostMapping("upload")
    public R<SysFile> upload(MultipartFile file)
    {
        return doUpload(file);
    }

    /**
     * 图片上传请求，兼容旧业务接口返回字段。
     */
    @PostMapping("uploadImg")
    public R<SysFile> uploadImg(MultipartFile file, @RequestParam(required = false) String business)
    {
        return doUpload(file);
    }

    private R<SysFile> doUpload(MultipartFile file)
    {
        try
        {
            // 上传并返回访问地址
            String url = sysFileService.uploadFile(file);
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            sysFile.setImgID(url);
            sysFile.setFileUrl(url);
            return R.ok(sysFile);
        }
        catch (Exception e)
        {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }
}
