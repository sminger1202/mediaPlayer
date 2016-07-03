package com.youku.player.unicom;

/**
 * Created by malijie on 2014/12/16.
 * 联通3G返回视频信息
 */
public class ChinaUnicomVideoInfo {
    private String cancelTime;
    private String endTime;
    private String orderTime;
    private String productName;
    private int status;
    private int type;
    private String vedioId;
    private String vedioName;
    private String vedioTag;
    private String vedioUrl;
    private String vedioImage;
    private String price;

    public String getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(String cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVedioId() {
        return vedioId;
    }

    public void setVedioId(String vedioId) {
        this.vedioId = vedioId;
    }

    public String getVedioName() {
        return vedioName;
    }

    public void setVedioName(String vedioName) {
        this.vedioName = vedioName;
    }

    public String getVedioUrl() {
        return vedioUrl;
    }

    public void setVedioUrl(String vedioUrl) {
        this.vedioUrl = vedioUrl;
    }

    public String getVedioTag() {
        return vedioTag;
    }

    public void setVedioTag(String vedioTag) {
        this.vedioTag = vedioTag;
    }

    public String getVedioImage() {
        return vedioImage;
    }

    public void setVedioImage(String vedioImage) {
        this.vedioImage = vedioImage;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "WoVedioInfo{" +
                "cancelTime='" + cancelTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", productName='" + productName + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", vedioId='" + vedioId + '\'' +
                ", vedioName='" + vedioName + '\'' +
                ", vedioTag='" + vedioTag + '\'' +
                ", vedioUrl='" + vedioUrl + '\'' +
                ", vedioImage='" + vedioImage + '\'' +
                ", price=" + price +
                '}';
    }
}
