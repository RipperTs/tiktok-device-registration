package com.douyin.spider.jni;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.Symbol;
import com.github.unidbg.linux.android.AndroidARMEmulator;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.MemoryBlock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class JniDispatchRegister extends AbstractJni {

    private static String soPath = "dylib/libEncryptor.so";
    private static String soName = "soName";
    private static String className = "com/bytedance/frameworks/encryptor/EncryptorUtil";
    private static String methodSign = "ttEncrypt([BI)[B";
    private static Logger logger = LoggerFactory.getLogger(JniDispatchRegister.class);
    private static LibraryResolver createLibraryResolver() {
        return new AndroidResolver(23);
    }

    private static AndroidEmulator createARMEmulator() {
        return new AndroidARMEmulator("com.sun.jna");
    }

    private final AndroidEmulator emulator;
    private final Module module;
    private final VM vm;

    private final DvmClass Native;

    public JniDispatchRegister(){

        emulator = createARMEmulator();
        final Memory memory = emulator.getMemory();
        memory.setLibraryResolver(createLibraryResolver());

        vm = emulator.createDalvikVM(null);
        vm.setJni(this);
        vm.setVerbose(true);
        ClassPathResource resource = new ClassPathResource(soPath);
        File soFile = null;
        InputStream inputStream = null;
        try {
            inputStream = resource.getInputStream();
            File tempFile = File.createTempFile(soName, ".so");
            FileUtils.copyInputStreamToFile(inputStream,tempFile);
            soFile = tempFile;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(inputStream);
        }
//        File soFile = null;
//        try {
//            soFile = ResourceUtils.getFile("classpath:" + soPath);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        File soFile = new File("D:\\CodeWorkSpace\\spiderWorkSpace\\unidbgProject\\unidbg-android\\src\\main\\resources\\dylib\\libEncryptor.so");
        DalvikModule dm = vm.loadLibrary(soFile, false);
        dm.callJNI_OnLoad(emulator);
        this.module = dm.getModule();

        Native = vm.resolveClass(className);

        Symbol __system_property_get = module.findSymbolByName("__system_property_get", true);
        MemoryBlock block = memory.malloc(0x10);
        Number ret = __system_property_get.call(emulator, "ro.build.version.sdk", block.getPointer())[0];
        logger.info("sdk=" + new String(block.getPointer().getByteArray(0, ret.intValue())));
    }

    private void destroy() {
        try {
            emulator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        JniDispatchRegister test = new JniDispatchRegister();

        test.test();

        test.destroy();
    }

    public byte[] callttEncryptMethod(byte[] data){
//        Native.callStaticJniMethod(emulator, methodSign, data,data.length);
        Object ret = Native.callStaticJniMethodObject(emulator, methodSign, new ByteArray(vm,data),data.length);
        byte[] tt = (byte[]) ((DvmObject) ret).getValue();
        return tt;
    }

    private void test() throws Exception {

//        Map<String, Object> deviceParams = DouYinRegisterInfo.getDeviceParams();
//        JSONObject registerInfo = DouYinRegisterInfo.getRegisterInfo(deviceParams);
//        byte[] data = compress(registerInfo.toString(),"utf-8");
////        byte[] data = new byte[]{100, 101, 118, 105, 99, 101, 95, 116, 121, 112, 101, 61, 79, 78, 69, 80, 76, 85, 83, 32, 65, 51, 48, 48, 48, 38, 100, 101, 118, 105, 99, 101, 95, 98, 114, 97, 110, 100, 61, 79, 110, 101, 80, 108, 117, 115};
////        int time = (int) (System.currentTimeMillis() / 1000);
//        byte[] tt = callttEncryptMethod(data);
//
////        logger.info("string 类型"+ Arrays.toString(tt));
////        logger.info("byte 【"+ Arrays.toString((byte[]) ((DvmObject) ret).getValue()));
//        String resp = sendPost(deviceParams, tt);
//        JSONObject jsonObject = new JSONObject(resp);
//
//        Map<String, Object> stringObjectMap = registerInfo.getJSONObject("header").toMap();
//        Map<String, Object> device_message = jsonObject.toMap();
//        for(String key :device_message.keySet()){
//            if(key.equals("install_id_str")){
//                stringObjectMap.put("iid",device_message.get(key));
//            }
//            stringObjectMap.put(key,device_message.get(key));
//        }
//        deviceParams.putAll(stringObjectMap);
//        deviceParams.put("uuid",deviceParams.get("udid"));
//        logger.info("【device param 】"+new JSONObject(deviceParams));
//
//        deviceParams.put("sim_serial_number","89860042191573218601");
//        // 激活
////        app_alert(deviceParams);
//        // 调用列表数据
//        String url ="https://aweme.snssdk.com/aweme/v1/aweme/post/?source=0&max_cursor=0&sec_user_id=MS4wLjABAAAAryNwJa_HtclngdC-xXIKvG8liaveo5h4kiOjIBt5I1ml76MM-WRniwqzWDIZREl9&count=20&os_api=23&device_type=ONEPLUS%20A3000&ssmix=a&manifest_version_code=110500&dpi=480&uuid=863581039914077&app_name=douyin_lite&version_name=11.5.0&ts=1600745725&app_type=normal&ac=wifi&update_version_code=11509900&channel=oppo&_rticket=1600745727371&device_platform=android&iid=3518051136579950&version_code=110500&mac_address=C0%3AEE%3AFB%3AE0%3A31%3A89&cdid=dfd60f13-53ca-42eb-8af9-d187f5ca2edc&openudid=a9fc3bd6e5c99814&device_id=39796983352&resolution=1080*1920&os_version=6.0.1&language=zh&device_brand=OnePlus&aid=2329";
//        url = URLUtil.replaceParam(url, deviceParams);
//        XgorgonService xgorgonService = new XgorgonService();
//        Map<String, Object> map = xgorgonService.generateXGorgon(url);
//        XgorgonService.getVideoList(url,map.get("X-Gorgon").toString(),map.get("X-Khronos").toString());
//

    }
}
