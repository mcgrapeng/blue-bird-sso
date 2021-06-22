package com.bird.sso.web.controller;

import com.bird.RES;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.conts.Constants;
import com.bird.sso.service.ApkService;
import com.bird.sso.utils.UploadUtils;
import com.bird.sso.web.conts.ResultEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/7 11:13
 */
@Slf4j
@Controller
public class APKController {

    @Autowired
    private UploadUtils uploadUtils;

    @Autowired
    private ApkService apkService;


    /**
     * apk升级
     *
     * @return
     */
    @RequestMapping(value = {"/sso/open-query/appVersion", "/open-query/appVersion"}, method = RequestMethod.POST)
    @ResponseBody
    public RES<com.bird.sso.domain.AppVersion> appVersion(@Validated(value = {AppVersion.apkVersion.class}) AppVersion aVersion) throws Exception {

        com.bird.sso.domain.AppVersion maxVersion = apkService.getNextVersion(aVersion.getPackageName()
                , aVersion.getVersionCode());
        return RES.of(ResultEnum.处理成功.code, maxVersion, ResultEnum.处理成功.name());
    }


    /**
     * 上传apk
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/open-query/apk", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> apk(@Validated(value = {AppVersion.apk.class}) AppVersion aVersion
            , MultipartFile file) throws Exception {

        boolean check = UploadUtils.isFileName(Objects.requireNonNull(file.getOriginalFilename()), ".apk");
        if (!check) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }

        String key = uploadUtils.uploadFile(Constants.bird_OSS, file);

        apkService.createApkVersion(key, file.getOriginalFilename(), aVersion.getPackageName()
                , aVersion.getVersionCode(), aVersion.getDescribe(), aVersion.getIsForce());

        return RES.of(ResultEnum.处理成功.code, key, ResultEnum.处理成功.name());
    }


    @RequestMapping(value = {"/open-query/download-apk", "/sso/open-query/download-apk"}, method = RequestMethod.GET)
    @ResponseBody
    public RES<String> download(@RequestParam(value = "_key") String key, HttpServletResponse response) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(key)) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }

        String versionCode = apkService.getLatestVersionCode(key);

        if (StringUtils.isBlank(versionCode)) {
            throw SSOException.NULL;
        }

        response.setHeader("content-disposition",
                "attachment;filename=" + URLEncoder.encode("police.party.oneclick." + versionCode + ".apk", "UTF-8"));

        uploadUtils.download(key, response);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    @RequestMapping(value = {"/open-query/latest-download-version", "/sso/open-query/latest-download-version"}, method = RequestMethod.GET)
    @ResponseBody
    public RES<String> downloadUrl(@RequestParam(value = "_package_name", required = false) String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }
        return RES.of(ResultEnum.处理成功.code, apkService.getLatestVersion(packageName), ResultEnum.处理成功.name());
    }


    @Setter
    @Getter
    static class AppVersion {
        //版本号
        @NotBlank(message = "版本号不能为空", groups = {apk.class, apkVersion.class})
        private String versionCode;
        //apk路径
        private String downloadUrl;
        //升级信息
        @NotBlank(message = "版本描述不能为空", groups = {apk.class})
        private String describe;

        //@NotBlank(message = "版本签名不能为空", groups = {apk.class})
        private String md5;

        private String isForce = PublicEnum.Y.name();
        //包名
        @NotBlank(message = "包名不能为空", groups = {apk.class, apkVersion.class})
        private String packageName;

        interface apk {
        }

        ;

        interface apkVersion {
        }

        ;
    }
}
