package cn.vicky.engine.classloader.core.exception;

import cn.vicky.engine.classloader.core.ResourceType;

/**
 * 资源无法找到异常
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public class ResourceNotFoundException extends JclException {
    /**
     * Default serial id
     */
    private static final long serialVersionUID = 1L;

    private String resourceName;
    private ResourceType resourceType;

    /**
     * Default constructor
     */
    public ResourceNotFoundException() {
        super();
    }

    /**
     * @param message
     */
    public ResourceNotFoundException(String message) {
        super( message );
    }

    /**
     * @param resource
     * @param message
     */
    public ResourceNotFoundException(String resource, String message) {
        super( message );
        resourceName = resource;
        determineResourceType( resource );
    }

    /**
     * @param e
     * @param resource
     * @param message
     */
    public ResourceNotFoundException(Throwable e, String resource, String message) {
        super( message, e );
        resourceName = resource;
        determineResourceType( resource );
    }

    /**
     * @param resourceName
     */
    private void determineResourceType(String resourceName) {
        if( resourceName.toLowerCase().endsWith( "." + ResourceType.CLASS.name().toLowerCase() ) )
            resourceType = ResourceType.CLASS;
        else if( resourceName.toLowerCase().endsWith( "." + ResourceType.PROPERTIES.name().toLowerCase() ) )
            resourceType = ResourceType.PROPERTIES;
        else if( resourceName.toLowerCase().endsWith( "." + ResourceType.XML.name().toLowerCase() ) )
            resourceType = ResourceType.XML;
        else
            resourceType = ResourceType.UNKNOWN;
    }

    /**
     * @return {@link ResourceType}
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * @param resourceName
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * @return {@link ResourceType}
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceType
     */
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
