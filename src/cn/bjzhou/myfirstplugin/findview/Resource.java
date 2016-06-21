package cn.bjzhou.myfirstplugin.findview;

public class Resource {
    private String resourceId;
    private String resourceType;
    private boolean isAndroidId = false;

    public boolean isAndroidId() {
        return isAndroidId;
    }

    public void setAndroidId(boolean androidId) {
        isAndroidId = androidId;
    }

    public Resource(String resourceId, String resourceType) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}