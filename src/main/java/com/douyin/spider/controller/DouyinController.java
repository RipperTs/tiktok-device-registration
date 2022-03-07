package com.douyin.spider.controller;

import com.douyin.spider.service.RegisterService;
import com.douyin.spider.service.XGorgonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    public Map<String,Object> register() throws IOException {
        return registerService.register();
    }

}
