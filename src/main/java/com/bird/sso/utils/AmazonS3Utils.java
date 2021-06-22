package com.bird.sso.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class AmazonS3Utils {


    @Value("${bird.s3.accessKey}")
    private String accessKeyID = "i5Kxj3i0FmoNn2Vl1g4J";

    @Value("${bird.s3.secretKey}")
    private String secretKey = "FWFAO9EYJafbcuqKW6oNqTPsxtUhQv1AT2aY2rZ6";

    @Value("${bird.s3.eweBucket}")
    private String bucketName = "bird-oss";

    @Value("${bird.s3.endPoint}")
    private String endpoint = "http://pub-shbt.s3.360.cn" /*= "http://bjcc.s3.addops.soft.360.cn"*/;

    private AWSCredentials credentials;
    private AmazonS3 s3Client;

    private void getInit() {

        // 初始化
        if (s3Client == null) {

            // 创建Amazon S3对象
            credentials = new BasicAWSCredentials(accessKeyID, secretKey);

            // 设置S3超时时间
            int timeoutConnection = 30000;
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setMaxErrorRetry(0);// 不重试
            clientConfiguration.setConnectionTimeout(timeoutConnection);
            clientConfiguration.setSocketTimeout(timeoutConnection);
            clientConfiguration.setProtocol(Protocol.HTTP);

            s3Client = new AmazonS3Client(credentials, clientConfiguration);

            S3ClientOptions clientOptions = new S3ClientOptions();
            clientOptions.setPathStyleAccess(true);
            clientOptions.disableChunkedEncoding();

            s3Client.setEndpoint(endpoint);
            s3Client.setS3ClientOptions(clientOptions);

            // 设置区域
            // s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2));
        }
    }

    /**
     * 查看所有可用的bucket
     */
    public void getAllBucket(AmazonS3 s3Client) {
        List<Bucket> buckets = s3Client.listBuckets();
        for (Bucket bucket : buckets) {
            System.out.println("Bucket: " + bucket.getName());
        }
    }

    /**
     * 查看bucket下所有的object
     */
    public void getAllBucketObject(AmazonS3 s3Client) {

        // 初始化
        getInit();
        ObjectListing objects = s3Client.listObjects(bucketName);
        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                System.out.println("Object: " + objectSummary.getKey());
            }
            objects = s3Client.listNextBatchOfObjects(objects);

        } while (objects.isTruncated());
    }

    /**
     * 获取URL
     */
    public String getUrl(String key) {

        // 初始化
        getInit();

        // 文件URL
        URL url = s3Client.getUrl(bucketName, key);

        // 返回URL
        return url.toString();
    }

    /**
     * amazonS3文件上传
     */
    public void upload(String key, byte[] data) {
        // 文件上传
        getInit();

        // 元数据
        ObjectMetadata metadata = new ObjectMetadata();

        // 数据长度
        metadata.setContentLength(data.length);

        // 上传到S3
        PutObjectResult result = s3Client.putObject(bucketName, key, new ByteArrayInputStream(data), metadata);

        // 判断是否成功
        if (StringUtils.isNotEmpty(result.getETag())) {
            log.info("上传文件到 Amazon S3 - 成功, ETag 是 " + result.getETag());
        }
        log.info("上传文件到 Amazon S3 - 结束");
    }

    /**
     * amazonS3文件下载
     */
    public void download(String key, HttpServletResponse response) {

        // 初始化
        getInit();

        // 下载文件
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));

        // 如果有文件
        if (object != null) {

            InputStream input = null;

            OutputStream out = null;
            byte[] data;
            try {

                System.out.println("--------------------" + key + "--------------------------");
//                // 获取文件流
//                // 信息头，相当于新建的名字

                response.setHeader("content-disposition",
                        "attachment;filename=" + URLEncoder.encode(key, "UTF-8"));
//                response.setContentType("multipart/form-data");
////                response.setContentType("image/jpeg"); // 设置返回内容格式
                response.setContentType("multipart/form-data");
                input = object.getObjectContent();
                data = new byte[input.available()];
                int len = 0;
                out = response.getOutputStream();

                while ((len = input.read(data)) != -1) {
                    out.write(data, 0, len);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                // 关闭输入输出流
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * amazonS3文件下载base64string
     */
    public String downloadBase64String(String key) {
        // 初始化
        getInit();

        // 下载文件
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));

        // 如果有文件
        if (object != null) {

            InputStream input = null;
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();

            byte[] data = null;
            try {

                // 数据入流
                input = object.getObjectContent();
                data = new byte[input.available()];
                int len = 0;

                while ((len = input.read(data)) != -1) {
                    swapStream.write(data, 0, len);
                }

                String encodedString = Base64.getEncoder().encodeToString(swapStream.toByteArray());

                return encodedString;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭输入输出流
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        } else {
            log.debug("----------没找到文件----------------");
        }

        // 没找到返回空
        return null;
    }

    /**
     * 文件删除
     */
    public void amazonS3DeleteObject(String key) {

        // 初始化
        getInit();

        // 删除桶中数据
        s3Client.deleteObject(bucketName, key);
    }

    /**
     * 删除存储桶
     */
    public void amazonS3DeleteBucket(AmazonS3 s3Client, String bucketName) {
        s3Client.deleteBucket(bucketName);
    }

    /**
     * 多线程大文件上传
     */
    @SuppressWarnings("unused")
    public void uploadBigFile(String localPath, String key) throws InterruptedException {

        // 在文件比较大的时候，有必要用多线程上传
        int threshold = 4 * 1024 * 1024;

        TransferManager tm = new TransferManager(s3Client);
        TransferManagerConfiguration conf = tm.getConfiguration();

        conf.setMultipartUploadThreshold(threshold);
        tm.setConfiguration(conf);

        Upload upload = tm.upload(bucketName, key, new File(localPath));
        TransferProgress p = upload.getProgress();

        while (upload.isDone() == false) {
            int percent = (int) (p.getPercentTransfered());
            Thread.sleep(500);
        }

        // 默认添加public权限
        s3Client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
    }

    public byte[] downloadByte(String key) {
        // 初始化
        getInit();

        // 下载文件
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));

        // 如果有文件
        if (object != null) {

            InputStream input = null;
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();

            byte[] data = null;
            try {

                // 数据入流
                input = object.getObjectContent();
                data = new byte[input.available()];
                int len = 0;

                while ((len = input.read(data)) != -1) {
                    swapStream.write(data, 0, len);
                }

                return swapStream.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭输入输出流
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        } else {
            log.debug("----------没找到文件----------------");
        }

        // 没找到返回空
        return null;
    }

    /**
     * 压缩留数据
     */
    public byte[] generateThumbnail(MultipartFile file) throws IOException {
        // 构建输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            // 压缩图片
            Thumbnails.of(file.getInputStream()).scale(0.5f).toOutputStream(outputStream);

        } catch (IOException e) {
            e.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return outputStream.toByteArray();
    }


    public String getBucketName() {
        return bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
