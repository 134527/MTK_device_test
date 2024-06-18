package com.DeviceTest.helper;

import android.os.SystemProperties;
import android.text.TextUtils;

public class ConfigUtil {
    public static int ANDROID_SDK_VERSION_P = 28;
    public static int ANDROID_SDK_VERSION_R = 30;
    public static int ANDROID_SDK_VERSION_S = 31;

    private static String mProductModel;

    public static boolean isSpecialEvb() {
        String platform = SystemProperties.get("ro.board.platform");
        if ("rk356x".equals(platform)) {
            if (TextUtils.isEmpty(mProductModel)) {
//                mProductModel = SystemUtil.execShellCmd(
//                        "cat /sys/firmware/devicetree/base/model");
                mProductModel = "EVB";
            }
            if (null != mProductModel) {
                return mProductModel.contains("EVB");
            }
        }
        return false;
    }
}
