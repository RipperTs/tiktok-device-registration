package com.douyin.spider.util;

import org.json.JSONObject;

import java.util.*;

/**
 * 整理抖音设备注册信息
 */
public class DouYinRegisterInfo {

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description //TODO 设备信息初始化
     * @Param []
     **/
    public static Map<String, Object> getDeviceParams() {
        String uuid = "3529" + String.valueOf(new Random().nextInt(999)) + (new Random().nextInt(899999999) + 100000000);
        long time = System.currentTimeMillis();
        Map<String, Object> deviceParams = new HashMap<>(32);
        deviceParams.put("ac", "wifi");
        deviceParams.put("aid", "2329");
        deviceParams.put("channel", "ky_douyinlite_and31"); //oppo
        deviceParams.put("device_brand", "Xiaomi");//OnePlus
        deviceParams.put("device_type", "MI%209"); //ONEPLUS%20A3000
        deviceParams.put("dpi", "320");
        deviceParams.put("language", "zh");
        deviceParams.put("manifest_version_code", "120500"); //110500
        deviceParams.put("os_api", "23");
        deviceParams.put("os_version", "6.0.1");
        deviceParams.put("update_version_code", "12509900"); //11509900
        deviceParams.put("version_code", "120500"); //110500
        deviceParams.put("version_name", "12.5.0"); //11.5.0
        deviceParams.put("uuid", uuid);
        deviceParams.put("openudid", UUID.randomUUID().toString().replaceAll("-", "").substring(16));
        deviceParams.put("mac_address", randomMac());
        deviceParams.put("cdid", UUID.randomUUID().toString());
        deviceParams.put("sim_serial_number", "");      // 89860042191573218602
        deviceParams.put("rom", "OnePlus");
        deviceParams.put("rom_version", "9");
        deviceParams.put("sdk_version", "9");
        deviceParams.put("cpu_abi", "arm64-v8a");
        deviceParams.put("device_model", "MI%209"); //  ONEPLUS%20A3000
        deviceParams.put("resolution", "1080*1920");
        deviceParams.put("_rticket", time);
        deviceParams.put("ts", time / 1000);
        return deviceParams;
    }


    /**
     * @return org.json.JSONObject
     * @Description //TODO 设备信息注册请求体
     * @Param [deviceParams]
     **/
    public static JSONObject getRegisterInfo(Map<String, Object> deviceParams) {

        Map<String, String> temp = new HashMap<>(16);
        if (deviceParams.containsKey("sim_serial_number")) {
            String sim_serial_number = deviceParams.get("sim_serial_number").toString();
            temp.put("sim_serial_number", sim_serial_number);
        } else {
            temp.put("sim_serial_number", ""); //89860118841817353175
        }
        List<Map<String, String>> simSerialNumber = new ArrayList<>();
        simSerialNumber.add(temp);

        JSONObject header = new JSONObject();
        header.put("display_name", "抖音短视频");
        header.put("update_version_code", deviceParams.get("update_version_code"));
        header.put("manifest_version_code", deviceParams.get("manifest_version_code"));
        header.put("aid", deviceParams.get("aid"));
        // "aweGW"
        header.put("channel", deviceParams.get("channel"));
        header.put("appkey", "57bfa27c67e58e7d920028d3");
        header.put("package", "com.ss.android.ugc.aweme");
        header.put("app_version", deviceParams.get("version_name"));
        header.put("version_code", deviceParams.get("version_code"));
        // 手机硬件相关
        header.put("sdk_version", deviceParams.get("sdk_version"));
        header.put("os", "Android");
        header.put("os_version", deviceParams.get("os_version"));
        header.put("os_api", deviceParams.get("os_api"));
        // "Redmi 5A"
        header.put("device_model", deviceParams.get("device_type"));
        //"Xiaomi"
        header.put("device_brand", deviceParams.get("device_brand"));
        //"Xiaomi"
        header.put("device_manufacturer", deviceParams.get("device_brand"));
        header.put("cpu_abi", deviceParams.get("cpu_abi"));
        header.put("build_serial", "3e04d6367cf5");
        header.put("release_build", "eed1c7d_20191112");
        header.put("density_dpi", deviceParams.get("dpi"));
        header.put("display_density", "xhdpi");
        header.put("resolution", deviceParams.get("resolution"));
        header.put("language", "zh");
        header.put("mc", deviceParams.containsKey("mac_address") ? deviceParams.get("mac_address") : randomMac());
        header.put("timezone", 8);
        header.put("access", deviceParams.get("ac"));
        header.put("not_request_sender", 0);
        //手机硬件相关
        header.put("rom", deviceParams.get("rom"));
        //手机硬件相关
        header.put("rom_version", deviceParams.get("rom_version"));
        header.put("sig_hash", "aea615ab910015038f73c47e45d21466");
        header.put("openudid", deviceParams.containsKey("openudid") ? deviceParams.get("openudid") : UUID.randomUUID().toString().replaceAll("-", "").substring(16));
        header.put("udid", deviceParams.containsKey("uuid") ? deviceParams.get("uuid") : "868374033134180");
        header.put("clientudid", UUID.randomUUID().toString());
        header.put("serial_number", ""); //3e04d6367cf5
        header.put("sim_serial_number", simSerialNumber);
        header.put("region", "CN");
        header.put("tz_name", "Asia/Shanghai");
        header.put("tz_offset", 28800);
        header.put("sim_region", "cn");
        JSONObject registerInfo = new JSONObject();
        registerInfo.put("magic_tag", "ss_app_log");
        registerInfo.put("header", header);
        registerInfo.put("_gen_time", deviceParams.get("ts"));
        return registerInfo;

    }

    private static String SEPARATOR_OF_MAC = ":";

    /**
     * Generate a random MAC address for qemu/kvm
     * 52-54-00 used by qemu/kvm
     * The remaining 3 fields are random,  range from 0 to 255
     *
     * @return MAC address string
     */
    public static String randomMac() {
        Random random = new Random();
        String[] mac = {
                String.format("%02x", 0x52),
                String.format("%02x", 0x54),
                String.format("%02x", 0x00),
                String.format("%02x", random.nextInt(0xff)),
                String.format("%02x", random.nextInt(0xff)),
                String.format("%02x", random.nextInt(0xff))
        };
        return String.join(SEPARATOR_OF_MAC, mac);
    }

    public static void main(String[] args) {

    }
}
