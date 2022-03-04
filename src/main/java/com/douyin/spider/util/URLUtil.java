package com.douyin.spider.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @description：
 * @modified By：wslmf
 * @version: $
 */
public class URLUtil {
    private static Logger logger = LoggerFactory.getLogger(URLUtil.class);

    public static String replaceParam(String url, Map<String,Object> device_params){
//        logger.info("【替换前URL】"+url);
        String[] params = url.substring(url.indexOf("?") + 1).split("&");
        for(int i = 0;i< params.length;i++ ){
            String[] split1 = params[i].split("=");
            if(device_params.containsKey(split1[0])){
                split1[1] = device_params.get(split1[0]).toString();
            }
            params[i] = split1[0]+"="+ URLEncoder.encode(split1[1]);
        }
        String uri = String.join("&",params);
        String returnUrl = url.substring(0,url.indexOf("?")+1)+uri;
//        logger.info("【替换后URL】"+returnUrl);
        return returnUrl;
    }

}
