package com.douyin.spider.controller;

import com.douyin.spider.jni.JniDispatchRegister;
import com.douyin.spider.service.RegisterService;
import com.douyin.spider.service.XGorgonService;
import com.douyin.spider.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @description：采集对外接口
 * @modified By：wslmf
 * @version: $
 */
@RestController
public class DouyinController {

    private static Logger logger = LoggerFactory.getLogger(DouyinController.class);

    @Autowired
    RegisterService registerService;

    @Autowired
    XGorgonService xGorgonService;

    @RequestMapping(value = "register",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> register(){
        return registerService.register();
    }

    @RequestMapping(value = "getXGorgon",method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> getXGorgon(@RequestBody String url,
                                         @RequestParam(defaultValue = "false") Boolean isRegister){
        if(url == null || url.isEmpty()){
            return new HashMap<>();
        }
        if(isRegister){
            Map<String, Object> registerMessage = registerService.register();
            if (registerMessage == null) {
                return new HashMap<>();
            }
            url = URLUtil.replaceParam(url, registerMessage);
        }
        logger.info("【需要生成XG的URL】"+url);
        return xGorgonService.generateXGorgon(url);
    }

}
