package com.demo.demo;

/**
 * Author:BinarySatan
 * Time: 2018/2/2
 */

public class MagaDownloadEntity {

    public int type; //0正在下载  1下载完成 2暂停 3错误
    public int soFarBytes;
    public int totalBytes;
    public String itemId;

    public MagaDownloadEntity(String itemId, int type, int soFarBytes, int totalBytes) {
        this.itemId = itemId;
        this.type = type;
        this.soFarBytes = soFarBytes;
        this.totalBytes = totalBytes;
    }
}
