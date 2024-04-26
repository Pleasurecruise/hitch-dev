package com.heima.stroke.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.heima.commons.domin.bo.RoutePlanResultBO;
import com.heima.commons.domin.bo.TextValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BaiduMapClient {
    /*@Value("${baidu.map.api}")
    private String api;
    @Value("${baidu.map.ak}")
    private String ak;*/
    private String api = "https://api.map.baidu.com/routematrix/v2/driving?";
    private String ak = "S2BtxOhERHMliIH4YCEmLiypCLSJtJyq";
    private final static Logger logger = LoggerFactory.getLogger(BaiduMapClient.class);

    //TODO:任务3.2-调百度路径计算两点间的距离，和预估抵达时长
    public RoutePlanResultBO pathPlanning(String origins, String destinations) {

        Map params = new LinkedHashMap<String, String>();
        params.put("origins", origins);
        params.put("destinations", destinations);
        params.put("ak", ak);

        RoutePlanResultBO routePlanResultBO = new RoutePlanResultBO();
        try {
            String responseBody = requestGetAK(api, params);
            //解析返回的json数据
            JSONObject jsonObject = JSON.parseObject(responseBody);
            JSONObject result = jsonObject.getJSONArray("result").getJSONObject(0);
            TextValue distance = result.getJSONObject("distance").toJavaObject(TextValue.class);
            TextValue duration = result.getJSONObject("duration").toJavaObject(TextValue.class);
            //封装返回结果
            routePlanResultBO.setDistance(distance);
            routePlanResultBO.setDuration(duration);
            logger.info("请求百度地图API成功，返回结果：{}", responseBody);
        }catch (Exception e) {
            logger.error("请求百度地图API失败，错误信息：{}", e.getMessage());
        }
        return routePlanResultBO;
    }

    /**
     * 默认ak
     * 选择了ak，使用IP白名单校验：
     * 根据您选择的AK已为您生成调用代码
     * 检测到您当前的ak设置了IP白名单校验
     * 您的IP白名单中的IP非公网IP，请设置为公网IP，否则将请求失败
     * 请在IP地址为xxxxxxx的计算发起请求，否则将请求失败
     *
     * @return
     */
    public String requestGetAK(String strUrl, Map<String, String> param) throws Exception {
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return strUrl;
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        java.net.URL url = new URL(queryString.toString());
        System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();
        return buffer.toString();
    }
}
