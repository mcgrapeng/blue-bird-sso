package com.bird.sso.service;

import com.google.common.collect.Maps;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.AppVersion;
import com.bird.sso.mapper.AppVersionMapper;
import com.bird.sso.utils.AmazonS3Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/11/18 15:15
 */
@Service
public class ApkService {


    @Autowired
    private AppVersionMapper appVersionMapper;


    @Autowired
    private AmazonS3Utils amazonS3Utils;

    /**
     * 获取最新版本（根据包名）
     *
     * @param packageName
     * @return
     */
    public String getLatestVersion(String packageName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("packageName", packageName);
        AppVersion maxVersion = appVersionMapper.getMaxVersion(params);
        if (ObjectUtils.isEmpty(maxVersion)) {
            throw SSOException.NULL;
        }
        return maxVersion.getKey();
    }

    public String getLatestVersionCode(String key) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("key", key);
        AppVersion maxVersion = appVersionMapper.getMaxVersion(params);
        if (ObjectUtils.isEmpty(maxVersion)) {
            throw SSOException.NULL;
        }
        return maxVersion.getVersionCode();
    }


    public AppVersion getNextVersion(String packageName, String versionCode) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("packageName", packageName);
        params.put("versionCode", versionCode);
        AppVersion maxVersion = appVersionMapper.getNextVersion(params);
        return maxVersion;
    }


    private AppVersion getCurrentVersion(String packageName, String versionCode) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("packageName", packageName);
        params.put("versionCode", versionCode);
        AppVersion maxVersion = appVersionMapper.getBy(params);
        return maxVersion;
    }


    public void createApkVersion(String key, String fileName
            , String packageName, String versionCode
            , String describe, String isForce) {


        AppVersion currentVersion = getCurrentVersion(packageName, versionCode);
        if (ObjectUtils.isNotEmpty(currentVersion)) {
            currentVersion.setDescribe(describe);
            currentVersion.setKey(key);
            currentVersion.setOriginalFilename(fileName.substring(0, fileName.lastIndexOf(".")));
            currentVersion.setDownloadUrl(amazonS3Utils.getEndpoint() + "/" + amazonS3Utils.getBucketName() + "/" + key);
            currentVersion.setIsForce(StringUtils.isBlank(isForce) ? PublicEnum.Y.name() : isForce);

            currentVersion.setPackageName(packageName);
            currentVersion.setVersionCode(versionCode);

            appVersionMapper.update(currentVersion);
        } else {
            com.bird.sso.domain.AppVersion appVersion = new com.bird.sso.domain.AppVersion();
            appVersion.setDescribe(describe);
            appVersion.setKey(key);
            appVersion.setOriginalFilename(fileName.substring(0, fileName.lastIndexOf(".")));
            appVersion.setDownloadUrl(amazonS3Utils.getEndpoint() + "/" + amazonS3Utils.getBucketName() + "/" + key);
            appVersion.setIsForce(StringUtils.isBlank(isForce) ? PublicEnum.Y.name() : isForce);

            appVersion.setPackageName(packageName);
            appVersion.setVersionCode(versionCode);

            appVersionMapper.insert(appVersion);
        }
    }
}
