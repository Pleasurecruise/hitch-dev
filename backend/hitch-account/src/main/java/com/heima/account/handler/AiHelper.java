package com.heima.account.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.commons.enums.BusinessErrors;
import com.heima.commons.exception.BusinessRuntimeException;
import com.heima.modules.po.VehiclePO;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class AiHelper {
    /*@Value("${baidu.apikey}")
    private static String API_KEY;
    @Value("${baidu.secretkey}")
    private static String SECRET_KEY;**/

    public static final String API_KEY = "NHXQwOj1osj2OWSdYXOJ3pU8";
    public static final String SECRET_KEY = "tUvnfiOmpzcsYcVUhvRgbpbA583jmv3R";

    private final static Logger logger = LoggerFactory.getLogger(AiHelper.class);

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();


    public static void main(String []args) throws IOException {
        String code = new AiHelper().getLicense(null);
        System.out.println(code);
    }

    /*

    图像识别，获取车牌信息
    文档（行驶证识别）：https://cloud.baidu.com/doc/OCR/s/yk3h7y3ks
    文档（车牌识别）：https://cloud.baidu.com/doc/OCR/s/ck3h7y191
    获取车辆照片url
    将url下载到某个临时文件夹
    将文件编码为base64
    调百度AI接口，返回对应信息
    对比：行驶证车牌 和 车辆车牌是否一致
    如果一致，设置车牌信息，认证通过，身份变更为车主

    简化版业务流程（至少完成）：识别车辆车牌号即可

    * */
    public String getLicense(VehiclePO vehiclePO) throws IOException {
        String imageUrlString = vehiclePO.getCarFrontPhoto();
        URL imageUrl = new URL(imageUrlString);
        // 创建临时文件
        File tempFile = File.createTempFile("image", ".tmp");
        // 下载图片到临时文件
        FileUtils.copyURLToFile(imageUrl, tempFile);
        // 将文件内容编码为Base64
        String image = getFileContentAsBase64(tempFile.getAbsolutePath(), false);
        // 对图片进行编码
        String encodedImage = URLEncoder.encode(image, "UTF-8");
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        // image 可以通过 getFileContentAsBase64("C:\fakepath\BF5B05DAE4E94D74A69D54ECC20C1D1A.jpg") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
        RequestBody body = RequestBody.create(mediaType, "image=" + encodedImage + "&multi_detect=true&multi_scale=false");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println(responseBody);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String number = jsonNode.get("words_result").get(0).get("number").asText();
        return number;
    }

    /*
     * 获取文件base64编码
     *
     * @param path      文件路径
     * @param urlEncode 如果Content-Type是application/x-www-form-urlencoded时,传true
     * @return base64编码信息，不带文件头
     * @throws IOException IO异常
     */
    static String getFileContentAsBase64(String path, boolean urlEncode) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        String base64 = Base64.getEncoder().encodeToString(b);
        if (urlEncode) {
            base64 = URLEncoder.encode(base64, "utf-8");
        }
        return base64;
    }


    /*
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }

}
