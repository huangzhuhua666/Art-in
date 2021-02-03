package com.tw.artin.bean;

import java.util.List;

public class NewDeviceGroupBean {

    /**
     * address : string
     * bCreater : string
     * bModifier : string
     * companyInfoId : 0
     * createDate : 2021-01-07T01:18:37.587Z
     * devices : [{"address":0,"bCreater":"string","bModifier":"string","companyInfoId":0,"createDate":"2021-01-07T01:18:37.587Z","currentDeviceType":0,"deltaUV":0,"deviceGroupId":0,"deviceType":0,"deviceid":"string","effect":0,"hue":0,"id":0,"isPowserOn":true,"lightness":0,"model":"string","modifyDate":"2021-01-07T01:18:37.587Z","name":"string","order":0,"orginName":"string","preset":0,"saturation":0,"temperature":0,"userId":0}]
     * dgName : string
     * id : 0
     * modifyDate : 2021-01-07T01:18:37.587Z
     * order : 0
     * scenesId : 0
     * ungrouped : true
     */

    private String address;
    private String bCreater;
    private String bModifier;
    private int companyInfoId;
    private String createDate;
    private String dgName;
    private int id;
    private String modifyDate;
    private int order;
    private int scenesId;
    private boolean ungrouped;
    private List<DevicesBean> devices;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBCreater() {
        return bCreater;
    }

    public void setBCreater(String bCreater) {
        this.bCreater = bCreater;
    }

    public String getBModifier() {
        return bModifier;
    }

    public void setBModifier(String bModifier) {
        this.bModifier = bModifier;
    }

    public int getCompanyInfoId() {
        return companyInfoId;
    }

    public void setCompanyInfoId(int companyInfoId) {
        this.companyInfoId = companyInfoId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getDgName() {
        return dgName;
    }

    public void setDgName(String dgName) {
        this.dgName = dgName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getScenesId() {
        return scenesId;
    }

    public void setScenesId(int scenesId) {
        this.scenesId = scenesId;
    }

    public boolean isUngrouped() {
        return ungrouped;
    }

    public void setUngrouped(boolean ungrouped) {
        this.ungrouped = ungrouped;
    }

    public List<DevicesBean> getDevices() {
        return devices;
    }

    public void setDevices(List<DevicesBean> devices) {
        this.devices = devices;
    }

    public static class DevicesBean {
        /**
         * address : 0
         * bCreater : string
         * bModifier : string
         * companyInfoId : 0
         * createDate : 2021-01-07T01:18:37.587Z
         * currentDeviceType : 0
         * deltaUV : 0
         * deviceGroupId : 0
         * deviceType : 0
         * deviceid : string
         * effect : 0
         * hue : 0
         * id : 0
         * isPowserOn : true
         * lightness : 0
         * model : string
         * modifyDate : 2021-01-07T01:18:37.587Z
         * name : string
         * order : 0
         * orginName : string
         * preset : 0
         * saturation : 0
         * temperature : 0
         * userId : 0
         */

        private int address;
        private String bCreater;
        private String bModifier;
        private int companyInfoId;
        private String createDate;
        private int currentDeviceType;
        private int deltaUV;
        private int deviceGroupId;
        private int deviceType;
        private String deviceid;
        private int effect;
        private int hue;
        private int id;
        private boolean isPowserOn;
        private int lightness;
        private String model;
        private String modifyDate;
        private String name;
        private int order;
        private String orginName;
        private int preset;
        private int saturation;
        private int temperature;
        private int userId;

        public int getAddress() {
            return address;
        }

        public void setAddress(int address) {
            this.address = address;
        }

        public String getBCreater() {
            return bCreater;
        }

        public void setBCreater(String bCreater) {
            this.bCreater = bCreater;
        }

        public String getBModifier() {
            return bModifier;
        }

        public void setBModifier(String bModifier) {
            this.bModifier = bModifier;
        }

        public int getCompanyInfoId() {
            return companyInfoId;
        }

        public void setCompanyInfoId(int companyInfoId) {
            this.companyInfoId = companyInfoId;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public int getCurrentDeviceType() {
            return currentDeviceType;
        }

        public void setCurrentDeviceType(int currentDeviceType) {
            this.currentDeviceType = currentDeviceType;
        }

        public int getDeltaUV() {
            return deltaUV;
        }

        public void setDeltaUV(int deltaUV) {
            this.deltaUV = deltaUV;
        }

        public int getDeviceGroupId() {
            return deviceGroupId;
        }

        public void setDeviceGroupId(int deviceGroupId) {
            this.deviceGroupId = deviceGroupId;
        }

        public int getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(int deviceType) {
            this.deviceType = deviceType;
        }

        public String getDeviceid() {
            return deviceid;
        }

        public void setDeviceid(String deviceid) {
            this.deviceid = deviceid;
        }

        public int getEffect() {
            return effect;
        }

        public void setEffect(int effect) {
            this.effect = effect;
        }

        public int getHue() {
            return hue;
        }

        public void setHue(int hue) {
            this.hue = hue;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isIsPowserOn() {
            return isPowserOn;
        }

        public void setIsPowserOn(boolean isPowserOn) {
            this.isPowserOn = isPowserOn;
        }

        public int getLightness() {
            return lightness;
        }

        public void setLightness(int lightness) {
            this.lightness = lightness;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getModifyDate() {
            return modifyDate;
        }

        public void setModifyDate(String modifyDate) {
            this.modifyDate = modifyDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getOrginName() {
            return orginName;
        }

        public void setOrginName(String orginName) {
            this.orginName = orginName;
        }

        public int getPreset() {
            return preset;
        }

        public void setPreset(int preset) {
            this.preset = preset;
        }

        public int getSaturation() {
            return saturation;
        }

        public void setSaturation(int saturation) {
            this.saturation = saturation;
        }

        public int getTemperature() {
            return temperature;
        }

        public void setTemperature(int temperature) {
            this.temperature = temperature;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
}
