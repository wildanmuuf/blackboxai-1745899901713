package model;

public class Device {
    private String ip;
    private String mac;
    private String name;
    private String version;
    private String type;

    public Device(String ip, String mac, String name, String version, String type) {
        this.ip = ip;
        this.mac = mac;
        this.name = name;
        this.version = version;
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }
}
