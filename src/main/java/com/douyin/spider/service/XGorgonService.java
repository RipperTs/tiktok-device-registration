package com.douyin.spider.service;


import com.douyin.spider.jni.JniDispatchXGorgon;
import com.douyin.spider.util.URLUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @date ：Created in 2020/9/22 10:08
 * @description：X-gorgon签名获取
 * @modified By：wslmf
 * @version: $
 */
@Service
public class XGorgonService {
    private static Logger logger = LoggerFactory.getLogger(XGorgonService.class);

    @Autowired
    JniDispatchXGorgon jniXGorgon;

    /**
     * @Description //TODO 获得URL 参数列表的指定参数param
     * @Date 2020/9/22 10:24
     * @Param [url, param]
     * @return java.lang.String
     **/
    public static String getParam(String url,String param){
        int begin = url.indexOf("?")>0? url.indexOf("?")+1:0;
        String uri = url.substring(begin);
        String[] params = uri.split("&");
        for(String value:params){
            if(value.contains(param) && value.indexOf(param)==0){
                return value.substring(value.indexOf("=")+1);
            }
        }
        return "";
    }

    /**
     * @Description //TODO 根据URL生成X-gorgon
     * @Date 2020/9/22 10:36
     * @Param [url]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    public Map<String,Object> generateXGorgon(String url){
        // 根据uri加密后生成字符串
        String uri = url.substring(url.indexOf("?")+1);
        logger.debug("【uri】"+uri);
        String bodyData = getBodyData(uri);
        logger.debug("【body Data】"+bodyData);
        // 在调用so之前进行编码转化
        byte[] data = beforeEncode(bodyData);
        String ts = getParam(url, "ts");
        if(ts.isEmpty()){
            ts = String.valueOf(System.currentTimeMillis()/1000);
        }
        int time = Integer.valueOf(ts);
        logger.debug("【time】"+time);
        // 调用本地so文件
        byte[] tt = jniXGorgon.callLeviathanMethod(time, data);
        //在调用so之后进行解码转化
        String x_gorgon = afterDecode(tt);
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("X-Gorgon",x_gorgon);
        resp.put("X-Khronos",time);
        resp.put("url",url);
        logger.info("【resp】"+resp);
        return resp;
    }

    public static void getVideoList(String url,String x_gorgon,String x_khronos){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Host", "aweme-hl.snssdk.com")
                .addHeader("Connection", "keep-alive")
                .addHeader("X-SS-REQ-TICKET", getParam(url,"_rticket"))
                .addHeader("sdk-version", "1")
                .addHeader("User-Agent", "com.ss.android.ugc.aweme.lite/110500 (Linux; U; Android 6.0.1; zh_CN; ONEPLUS A3000; Build/MMB29M; Cronet/TTNetVersion:4df3ca9d 2019-11-25)")
                .addHeader("X-Gorgon", x_gorgon)
                .addHeader("X-Khronos", x_khronos)
                .build();
        try {
            Response response = client.newCall(request).execute();
            logger.debug(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @Description //TODO 根据uri加密后生成字符串
     * @Date 2020/9/22 10:36
     * @Param [uri]
     * @return java.lang.String
     **/
    public String getBodyData(String uri){
        JSONObject str = new JSONObject();
//        String uri = "source=0&max_cursor=0&sec_user_id=MS4wLjABAAAAxA44mxJVod_Aq5wc0cZrbZHJ2S_DnoJctGpb_mOvsxs&count=20&os_api=25&device_type=SM-G9650&ssmix=a&manifest_version_code=870&dpi=320&uuid=860046033047160&app_name=douyin_lite&version_name=8.7.0&ts=1600421993&app_type=normal&ac=wifi&update_version_code=8702&channel=aweGW&_rticket=1600421993106&device_platform=android&iid=738485302461095&version_code=870&mac_address=52:54:00:24:57:5f&cdid=c72cc213-26f1-40e2-aa7b-1aa41c01a502&openudid=ad9e4ed558079d9e&device_id=52205205146&resolution=1440*2960&os_version=7.1.2&language=zh&device_brand=samsung&aid=1128&mcc_mnc=46011";
        str.put("param",uri);
        str.put("stub","");
        str.put("cookie","");
        str.put("sessionid","");
        String param = str.getString("param");
        String stub = str.getString("stub");
        String cookie = str.getString("cookie");
        String sessionid = str.getString("sessionid");

        String str2 = "00000000000000000000000000000000";
        String md5 = (param == null ||param.length()<=0?str2 : DigestUtils.md5Hex(param.getBytes()));
        if(stub == null ||stub.length()<=0){
            stub = str2;
        }
        String md52 = (cookie == null ||cookie.length()<=0 ? str2 :DigestUtils.md5Hex(cookie.getBytes()));

        if(sessionid != null && sessionid.length() > 0){
            str2 = DigestUtils.md5Hex(sessionid.getBytes());
        }
        return md5+stub+md52+str2;
    }

    /**
     * @Description //TODO 在调用so之前进行编码转化
     * @Date 2020/9/22 10:37
     * @Param [str]
     * @return byte[]
     **/
    public byte[] beforeEncode(String str) {
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    /**
     * @Description //TODO  在调用so之后对Byte进行解码转化
     * @Date 2020/9/22 10:37
     * @Param [bArr]
     * @return java.lang.String
     **/
    public String afterDecode(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        char[] charArray = "0123456789abcdef".toCharArray();
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int b2 = bArr[i] & 255;
            int i2 = i * 2;
            cArr[i2] = charArray[b2 >>> 4];
            cArr[i2 + 1] = charArray[b2 & 15];
        }
        return new String(cArr);
    }

    public static void main(String[] args) throws IOException {
        RegisterService deviceService = new RegisterService();
        Map<String, Object> registerMessage = deviceService.register();
        if (registerMessage != null) {
            logger.info("【register Message】" + registerMessage);
        }
        String url ="https://aweme.snssdk.com/aweme/v1/aweme/post/?source=0&max_cursor=0&sec_user_id=MS4wLjABAAAAryNwJa_HtclngdC-xXIKvG8liaveo5h4kiOjIBt5I1ml76MM-WRniwqzWDIZREl9&count=20&os_api=23&device_type=ONEPLUS%20A3000&ssmix=a&manifest_version_code=110500&dpi=480&uuid=863581039914077&app_name=douyin_lite&version_name=11.5.0&ts=1600745725&app_type=normal&ac=wifi&update_version_code=11509900&channel=oppo&_rticket=1600745727371&device_platform=android&iid=3518051136579950&version_code=110500&mac_address=C0%3AEE%3AFB%3AE0%3A31%3A89&cdid=dfd60f13-53ca-42eb-8af9-d187f5ca2edc&openudid=a9fc3bd6e5c99814&device_id=39796983352&resolution=1080*1920&os_version=6.0.1&language=zh&device_brand=OnePlus&aid=2329";
        url = URLUtil.replaceParam(url, registerMessage);
        XGorgonService xgorgonService = new XGorgonService();
        Map<String, Object> stringObjectMap = xgorgonService.generateXGorgon(url);
        logger.info(stringObjectMap.toString());
        getVideoList(url,stringObjectMap.get("X-Gorgon").toString(),stringObjectMap.get("X-Khronos").toString());
    }

}
