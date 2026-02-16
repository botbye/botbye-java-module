package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotbyeExtraData implements Serializable {
    /**
     * The real IP address of the user. If a proxy is used, this will be the real IP address, otherwise, it will be the same as `ip`.
     * Note: This does not apply to VPN usage.
     */
    private final String realIp = null;
    private final String realCountry = null;
    /**
     * The IP address of the user. If a proxy is used, this will be the proxy IP address.
     */
    private String ip = null;
    private String asn = null;
    private String country = null;
    private String browser = null;
    private String browserVersion = null;
    private String deviceName = null; // Galaxy S9
    private String deviceType = null; // Mobile Phone
    private String deviceCodeName = null; // SM-G960F
    private String platform = null; // Android
    private String platformVersion = null; // 8

    public BotbyeExtraData() {
    }

    public BotbyeExtraData(
            String ip,
            String asn,
            String country,
            String browser,
            String browserVersion,
            String deviceName,
            String deviceType,
            String deviceCodeName,
            String platform,
            String platformVersion
    ) {
        this.ip = ip;
        this.asn = asn;
        this.country = country;
        this.browser = browser;
        this.browserVersion = browserVersion;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceCodeName = deviceCodeName;
        this.platform = platform;
        this.platformVersion = platformVersion;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceCodeName() {
        return deviceCodeName;
    }

    public void setDeviceCodeName(String deviceCodeName) {
        this.deviceCodeName = deviceCodeName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getRealIp() {
        return realIp;
    }

    public String getRealCountry() {
        return realCountry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotbyeExtraData that)) return false;
        return Objects.equals(realIp, that.realIp) && Objects.equals(realCountry, that.realCountry) && Objects.equals(ip, that.ip) && Objects.equals(asn, that.asn) && Objects.equals(country, that.country) && Objects.equals(browser, that.browser) && Objects.equals(browserVersion, that.browserVersion) && Objects.equals(deviceName, that.deviceName) && Objects.equals(deviceType, that.deviceType) && Objects.equals(deviceCodeName, that.deviceCodeName) && Objects.equals(platform, that.platform) && Objects.equals(platformVersion, that.platformVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(realIp, realCountry, ip, asn, country, browser, browserVersion, deviceName, deviceType, deviceCodeName, platform, platformVersion);
    }

    @Override
    public String toString() {
        return "BotbyeExtraData{" +
                "realIp='" + realIp + '\'' +
                ", realCountry='" + realCountry + '\'' +
                ", ip='" + ip + '\'' +
                ", asn='" + asn + '\'' +
                ", country='" + country + '\'' +
                ", browser='" + browser + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceCodeName='" + deviceCodeName + '\'' +
                ", platform='" + platform + '\'' +
                ", platformVersion='" + platformVersion + '\'' +
                '}';
    }
}
