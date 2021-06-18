package com.douyin.spider.jni;

import com.github.unidbg.*;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.*;
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
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


@Configuration
public class JniDispatchXGorgon extends AbstractJni {
    private static String soPath = "dylib/libcms.so";
    private static String soName = "libcms";
    private static String className = "com/ss/sys/ces/a";
    private static String methodSign = "leviathan(II[B)[B";
    private static Logger logger = LoggerFactory.getLogger(JniDispatchXGorgon.className);
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

    public JniDispatchXGorgon()  {
        emulator = createARMEmulator();
        final Memory memory = emulator.getMemory();
        memory.setLibraryResolver(createLibraryResolver());

        vm = emulator.createDalvikVM(null);
        vm.setJni(this);
        vm.setVerbose(true);
//        File soFile = null;
//        try {
//            soFile = ResourceUtils.getFile("classpath:" + soPath);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
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
        DalvikModule dm = vm.loadLibrary(soFile, false);
        dm.callJNI_OnLoad(emulator);
        this.module = dm.getModule();

        Native = vm.resolveClass(className);

        Symbol __system_property_get = module.findSymbolByName("__system_property_get", true);
        MemoryBlock block = memory.malloc(0x10);
        Number ret = __system_property_get.call(emulator, "ro.build.version.sdk", block.getPointer())[0];
        logger.info("sdk=" + new String(block.getPointer().getByteArray(0, ret.intValue())));
        try {
            Native.callStaticJniMethod(emulator, methodSign, -1,System.currentTimeMillis(),"".getBytes());
        }catch (Exception e){

        }
        logger.info("上述Native异常为必要初始化的操作，属于正常现象，请忽略");

    }

    private void destroy() throws IOException {
        emulator.close();
    }

    public byte[] callLeviathanMethod(int time,byte[] data){

//        Native.callStaticJniMethod(emulator, methodSign, -1,time,data);

        Object ret = Native.callStaticJniMethodObject(emulator, methodSign, -1,time,new ByteArray(vm,data));

        byte[] tt = (byte[]) ((DvmObject) ret).getValue();
        return tt;
    }


    public static void main(String[] args) throws Exception {

        JniDispatchXGorgon test = new JniDispatchXGorgon();

        test.destroy();
    }

}
