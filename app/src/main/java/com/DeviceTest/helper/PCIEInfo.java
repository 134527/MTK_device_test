package com.DeviceTest.helper;

public class PCIEInfo {
    private String staSpeed;
    private String staWidth;

    public String getStaSpeed() {
        return staSpeed;
    }

    public void setStaSpeed(String staSpeed) {
        this.staSpeed = staSpeed;
    }

    public String getStaWidth() {
        return staWidth;
    }

    public void setStaWidth(String staWidth) {
        this.staWidth = staWidth;
    }

    @Override
    public String toString() {
        String gen = staSpeed;
        if ("2.5GT/s".equals(staSpeed)) {
            gen = "gen1";
        } else if ("5GT/s".equals(staSpeed)) {
            gen = "gen2";
        } else if ("8GT/s".equals(staSpeed)) {
            gen = "gen3";
        }
        String lane = staWidth;
        if ("x1".equals(staWidth)) {
            lane = "1Lane";
        } else if ("x2".equals(staWidth)) {
            lane = "2Lanes";
        } else if ("x4".equals(staWidth)) {
            lane = "4Lanes";
        }
        return "gen='" + gen + '\'' +
                ", lane='" + lane + '\'';
    }
}