package com.DeviceTest.helper;

public class StorageInfo {
    private String path;
    private String sysPath;

    public StorageInfo(String path, String sysPath) {
        this.path = path;
        this.sysPath = sysPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSysPath() {
        return sysPath;
    }

    public void setSysPath(String sysPath) {
        this.sysPath = sysPath;
    }

    @Override
    public String toString() {
        return "StorageInfo{" +
                "path='" + path + '\'' +
                ", sysPath='" + sysPath + '\'' +
                '}';
    }
}
