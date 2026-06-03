package com.huimao.system.api.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 文件信息
 *
 * @author huimao
 */
@Data
public class SysFile
{
    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件地址
     */
    private String url;

    /**
     * 兼容旧上传接口的文件ID。
     */
    private String imgID;

    /**
     * 兼容旧上传接口的文件访问地址。
     */
    private String fileUrl;

}
