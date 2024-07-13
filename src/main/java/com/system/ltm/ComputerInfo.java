package com.system.ltm;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class ComputerInfo implements Serializable {
    private String username;
    private String hostname;
    private String ipAddress;
    private String osName;
    private String osVersion;
    private long totalRam;
    private long freeRam;
    private double cpuUsage;
    private long totalDiskSpace;
    private long freeDiskSpace;

    public ComputerInfo() {}

    public ComputerInfo(String username, String hostname, String ipAddress, String osName, String osVersion, long totalRam, long freeRam, double cpuUsage, long totalDiskSpace, long freeDiskSpace) {
        this.username = username;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.osName = osName;
        this.osVersion = osVersion;
        this.totalRam = totalRam;
        this.freeRam = freeRam;
        this.cpuUsage = cpuUsage;
        this.totalDiskSpace = totalDiskSpace;
        this.freeDiskSpace = freeDiskSpace;
    }

    public static ComputerInfo getComputerInfo(String username) {
        ComputerInfo info = new ComputerInfo();
        info.setUsername(username);
        try {
            info.setHostname(InetAddress.getLocalHost().getHostName());
            info.setIpAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        info.setOsName(System.getProperty("os.name"));
        info.setOsVersion(System.getProperty("os.version"));

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        info.setTotalRam(osBean.getTotalPhysicalMemorySize());
        info.setFreeRam(osBean.getFreePhysicalMemorySize());

        info.setCpuUsage(osBean.getSystemCpuLoad() * 100);

        File root = new File("/");
        info.setTotalDiskSpace(root.getTotalSpace());
        info.setFreeDiskSpace(root.getFreeSpace());
        return info;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public long getTotalRam() {
        return totalRam;
    }

    public void setTotalRam(long totalRam) {
        this.totalRam = totalRam;
    }

    public long getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(long freeRam) {
        this.freeRam = freeRam;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getTotalDiskSpace() {
        return totalDiskSpace;
    }

    public void setTotalDiskSpace(long totalDiskSpace) {
        this.totalDiskSpace = totalDiskSpace;
    }

    public long getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public void setFreeDiskSpace(long freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
    }

    @Override
    public String toString() {
        return String.format("User: %s, Hostname: %s, IP: %s, OS: %s %s, " +
                        "RAM: %.2f/%.2f GB, CPU: %.2f%%, Disk: %.2f/%.2f GB",
                username, hostname, ipAddress, osName, osVersion,
                (totalRam - freeRam) / 1e9, totalRam / 1e9,
                cpuUsage,
                (totalDiskSpace - freeDiskSpace) / 1e9, totalDiskSpace / 1e9);
    }
}