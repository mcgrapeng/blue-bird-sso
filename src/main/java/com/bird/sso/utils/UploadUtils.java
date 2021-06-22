package com.bird.sso.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/16 10:59
 */
@Slf4j
@Component
public final class UploadUtils {

    @Resource
    private AmazonS3Utils s3Util;

    private static final String DATA_FORMAT = "yyyyMMddHHmmss";


    public AmazonS3Utils getS3Util() {
        return s3Util;
    }


    /**
     * 获取网络路径
     * @param key
     * @return
     */
    public String getUrl(String key){
        return s3Util.getEndpoint() + "/" + s3Util.getBucketName() + "/" + key;
    }

    /**
     * 上传图片
     *
     * @param contextPath 路径
     * @param file
     * @return
     */
    public String uploadImage(String contextPath, final MultipartFile file) {
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

        if (StringUtils.isBlank(suffix)) {
            suffix = ".jpg";
        }
        //LocalDateTime localDate = LocalDateTime.now();
        String date = DateFormatUtils.format(new Date(), DATA_FORMAT);
        String key = contextPath.concat("/").concat(CommonUtils.getRandom(6))
                .concat("/").concat(date).concat(suffix);

        // 缩略图路径
        String thumbKey = "/" + "thumb" + key;
        try {
            //上传图片
            s3Util.amazonS3DeleteObject(key);
            s3Util.upload(key, file.getBytes());
            //上传缩略图
            s3Util.upload(thumbKey, s3Util.generateThumbnail(file));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return s3Util.getEndpoint() + "/" + s3Util.getBucketName() + "/" + key;
    }


    /**
     * 上传文件
     *
     * @param contextPath
     * @param file
     * @return
     */
    public String uploadFile(String contextPath, final MultipartFile file) {
        log.info("##################上传文件信息#############filename={}",file.getOriginalFilename());
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

        String date = DateFormatUtils.format(new Date(), DATA_FORMAT);
        String key = contextPath.concat("/").concat(CommonUtils.getRandom(6))
                .concat("/").concat(date).concat(suffix);
        try {
            s3Util.amazonS3DeleteObject(key);
            s3Util.upload(key, file.getBytes());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return key;
    }


    /**
     * 下载文件
     * @param key
     */
    public void download(String key) {
        log.info("##################下载文件信息，文件Key为 {}", key);
        s3Util.download(key,WebUtils.getResponse());
    }


    /**
     * 删除文件
     */
    public void delete(final String key) {
        log.info("##################删除文件信息，文件Key为 {}", key);
        //删除s3图片
        try {
            s3Util.amazonS3DeleteObject(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     * @param key
     */
    public void download(String key , HttpServletResponse response) {
        log.info("##################下载文件信息，文件Key为 {}", key);
        s3Util.download(key,response);
    }


    public static boolean isFileName(String fileName,String type){
        return fileName.toLowerCase().endsWith(type.toLowerCase());
    }

}
