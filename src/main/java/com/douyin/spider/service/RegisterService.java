package com.douyin.spider.service;
import com.douyin.spider.jni.JniDispatchRegister;
import com.douyin.spider.util.DouYinRegisterInfo;
import com.douyin.spider.util.URLUtil;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;


/**
 * @date ：Created in 2021/06/18日12:05:47
 * @description：设备注册类
 * @modified By：wslmf
 * @version: $
 */

@Service
public class RegisterService {
    private static Logger logger = LoggerFactory.getLogger(RegisterService.class);

    public static void main(String[] args) {
        RegisterService deviceService = new RegisterService();

        Map<String, Object> registerMessage = deviceService.register();
        if (registerMessage != null) {
            logger.info("【register Message】" + registerMessage);
        }
    }

    @Autowired
    JniDispatchRegister jniRegister;


    public Map<String, Object> register() {
        // 获得设备信息
        Map<String, Object> deviceParams = DouYinRegisterInfo.getDeviceParams();
        // 构建注册请求体
        JSONObject registerInfo = DouYinRegisterInfo.getRegisterInfo(deviceParams);
        logger.debug("【registerInfo】"+registerInfo);
        // 先将请求体进行压缩
        byte[] data = compress(registerInfo.toString(), "utf-8");
        // 对data进行加密
        byte[] tt = jniRegister.callttEncryptMethod(data);
//        logger.info("string 类型"+ Arrays.toString(tt));
//        logger.info("byte 【"+ Arrays.toString((byte[]) ((DvmObject) ret).getValue()));
        String registerResponse = callRegisterUrl(deviceParams, tt);
        if (registerInfo == null) {
            return null;
        }
        Map<String, Object> registerResponseMap = new JSONObject(registerResponse).toMap();
        if (!registerResponseMap.containsKey("device_id")) {
            return null;
        }

        Map<String, Object> bodyParams = registerInfo.getJSONObject("header").toMap();
//        Map<String, Object> device_message = registerResponseJson.toMap();
        // 将注册成功的数据合并到deviceParams
        for (String key : registerResponseMap.keySet()) {
            if (key.equals("install_id_str")) {
                deviceParams.put("iid", registerResponseMap.get(key));
            }
            deviceParams.put(key, registerResponseMap.get(key));
        }

        // 将请求体的数据合并到deviceParams
        deviceParams.putAll(bodyParams);
        // 替换sim_serial_number ，由于bodyParams 中是 数组格式无法使用，在这里进行转化
        deviceParams.put("sim_serial_number", registerInfo.getJSONObject("header").getJSONArray("sim_serial_number").getJSONObject(0).getString("sim_serial_number"));
        logger.info("【device param 】" + new JSONObject(deviceParams));
        // 如果new_user=1必须进行激活
        appAlert(deviceParams);
        return deviceParams;
    }


    /**
     * @return java.lang.String
     * @Description //TODO 调用注册接口
     * @Date 2020/9/21 18:44
     * @Param [deviceParam, ttbody]
     **/
    public String callRegisterUrl(Map<String, Object> deviceParam, byte[] requestBody) {

        String url = "https://log.snssdk.com/service/2/device_register/?ac=wifi&mac_address=C0%3AEE%3AFB%3AD5%3A4B%3A16&channel=oppo&aid=2329&app_name=douyin_lite&version_code=110500&version_name=11.5.0&device_platform=android&ssmix=a&device_type=ONEPLUS+A3000&device_brand=OnePlus&language=zh&os_api=23&os_version=6.0.1&uuid=860046033047160&openudid=d4080df15130f0d9&manifest_version_code=110500&resolution=1080*1920&dpi=480&update_version_code=11509900&_rticket=1600423440596&mcc_mnc=46011&ts=1600423440&app_type=normal&cdid=4dda0a2b-6572-418c-bdfa-89cfd78a3aaf&tt_data=a&os_api=23&device_type=ONEPLUS%20A3000&ssmix=a&manifest_version_code=110500&dpi=480&uuid=860046033047160&app_name=douyin_lite&version_name=11.5.0&ts=1600423440&app_type=normal&ac=wifi&update_version_code=11509900&channel=oppo&_rticket=1600423440787&device_platform=android&version_code=110500&mac_address=C0%3AEE%3AFB%3AD5%3A4B%3A16&cdid=4dda0a2b-6572-418c-bdfa-89cfd78a3aaf&openudid=d4080df15130f0d9&device_id=40178045261&resolution=1080*1920&os_version=6.0.1&language=zh&device_brand=OnePlus&aid=2329&mcc_mnc=46011";
        url = URLUtil.replaceParam(url, deviceParam);

        logger.debug("【注册url】" + url);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/octet-stream;tt-data=a");
        RequestBody body = RequestBody.create(mediaType, requestBody);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Host", "log.snssdk.com")
                .addHeader("Connection", "keep-alive")
                .addHeader("sdk-version", "1")
                .addHeader("Content-Type", "application/octet-stream;tt-data=a")
                .addHeader("User-Agent", "com.ss.android.ugc.aweme/870 (Linux; U; Android 7.1.2; zh_CN; SM-G9650; Build/N2G47H; Cronet/58.0.2991.0)")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String resp = response.body().string();
            logger.info("【注册返回结果】" + resp);
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return byte[]
     * @Description //TODO 注册前将请求体json进行压缩
     * @Date 2020/9/21 18:03
     * @Param [str, encoding]
     **/
    public byte[] compress(String str, String encoding) {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(8192);
        GZIPOutputStream gZIPOutputStream = null;
        try {
            gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(str.getBytes(encoding));
            gZIPOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Throwable th) {
            try {
                gZIPOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @Description //TODO 设备激活
     * @Date 2020/9/22 12:09
     * @Param [device_params]
     * @return void
     **/
    public void appAlert(Map<String,Object> device_params){
        String url = "https://ichannel.snssdk.com/service/2/app_alert_check/?build_serial=37c96a14&timezone=8.0&carrier=CHN-CT&mcc_mnc=46011&sim_region=cn&sim_serial_number=89860320750105912020&device_id=40178045261&ac=wifi&mac_address=C0%3AEE%3AFB%3AD5%3A4B%3A16&channel=oppo&aid=2329&app_name=douyin_lite&version_code=110500&version_name=11.5.0&device_platform=android&ssmix=a&device_type=ONEPLUS+A3000&device_brand=OnePlus&language=zh&os_api=23&os_version=6.0.1&uuid=860046033047160&openudid=d4080df15130f0d9&manifest_version_code=110500&resolution=1080*1920&dpi=480&update_version_code=11509900&_rticket=1600423440642&ts=1600423440&app_type=normal&cdid=4dda0a2b-6572-418c-bdfa-89cfd78a3aaf&req_id=7522ead3-00cb-435c-9376-5dd2e012bc71&os_api=23&device_type=ONEPLUS%20A3000&ssmix=a&manifest_version_code=110500&dpi=480&uuid=860046033047160&app_name=douyin_lite&version_name=11.5.0&ts=1600423440&app_type=normal&ac=wifi&update_version_code=11509900&channel=oppo&_rticket=1600423440803&device_platform=android&version_code=110500&mac_address=C0%3AEE%3AFB%3AD5%3A4B%3A16&cdid=4dda0a2b-6572-418c-bdfa-89cfd78a3aaf&openudid=d4080df15130f0d9&device_id=40178045261&resolution=1080*1920&os_version=6.0.1&language=zh&device_brand=OnePlus&aid=2329&mcc_mnc=46011";
        url = URLUtil.replaceParam(url, device_params);
        logger.debug("【激活URL】"+url);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Host", "ichannel.snssdk.com")
                .addHeader("Connection", "keep-alive")
                .addHeader("X-SS-REQ-TICKET", device_params.get("_rticket").toString())
//                .addHeader("X-Tt-Token", "0054151ee13867e63b34fceb2561855906969c4f162e9c5ceb4fa8923a01e8f767737d9e1aaa54e7877b190357b3247090a")
                .addHeader("sdk-version", "1")
//                .addHeader("x-tt-trace-id", "00-a0c903940995acc514d89fdbe82f0919-a0c903940995acc5-01")
                .addHeader("User-Agent", "com.ss.android.ugc.aweme.lite/110500 (Linux; U; Android 6.0.1; zh_CN; ONEPLUS A3000; Build/MMB29M; Cronet/TTNetVersion:4df3ca9d 2019-11-25)")
                .build();
        try {
            Response response = client.newCall(request).execute();
            logger.debug("【激活情况】"+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
