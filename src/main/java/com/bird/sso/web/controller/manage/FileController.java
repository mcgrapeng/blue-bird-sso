package com.bird.sso.web.controller.manage;

import com.google.common.collect.Maps;
import com.bird.RES;
import com.bird.sso.utils.UploadUtils;
import com.bird.sso.web.conts.ResultEnum;
import com.bird.sso.web.conts.WebConts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/3/11 15:14
 */
@RequestMapping("/sso/manage/file")
@Controller
public class FileController {

    @Autowired
    private UploadUtils uploadUtils;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public RES<Map<String, String>> upload(MultipartFile file) {
        Map<String, String> data = Maps.newHashMap();
        String key = uploadUtils.uploadFile(WebConts.bird_OSS, file);
        String url = uploadUtils.getUrl(key);
        data.put("key", key);
        data.put("url", url);
        data.put("filename", file.getOriginalFilename());
        return RES.of(ResultEnum.处理成功.code, data, ResultEnum.处理成功.name());
    }
}
