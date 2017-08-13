package com.jf.houspitalscaner.net.entity;

import com.haozi.baselibrary.net.entity.BaseNetEntity;

/**
 * Created by Android Studio.<br/>
 * ProjectName: cahl<br/>
 * Author: haozi<br/>
 * Date: 2017/5/26<br/>
 * Time: 14:32<br/>
 * --------------------------实例-------------------------------<br/>
 *  {"id":"402881985c3a9f00015c3a9f85fc0001","displayName":"head.png","fileUrl":"20151023/16149176-352f-4677-9413-a0ee8e000b8e.jpg","createName":"管理员","createBy":"admin","createDate":1495632152000,"updateName":null,"updateBy":null,"updateDate":null,"zipFileUrl":"20151023/16149176-352f-4677-9413-a0ee8e000b8e.jpg","relateKey":null}
 */
public class ImageEntity extends BaseNetEntity {

    private String id;
    private String displayName;
    private String fileUrl;
    private String createName;
    private String createBy;
    private long createDate;
    private String updateName;
    private String updateBy;
    private String updateDate;
    private String zipFileUrl;
    private String relateKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getZipFileUrl() {
        return zipFileUrl;
    }

    public void setZipFileUrl(String zipFileUrl) {
        this.zipFileUrl = zipFileUrl;
    }

    public String getRelateKey() {
        return relateKey;
    }

    public void setRelateKey(String relateKey) {
        this.relateKey = relateKey;
    }
}
