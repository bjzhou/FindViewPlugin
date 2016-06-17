package cn.bjzhou.myfirstplugin;

public class Resource {
    private String resourceId;
    private String resourceType;

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