package com.dbg.cloud.acheron.autoconfigure.admin;

import com.dbg.cloud.acheron.adminendpoints.AdminEndpoint;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class IgnoreAdminEndpointsHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return super.isHandler(beanType) && !AdminEndpoint.class.isAssignableFrom(beanType);
    }
}
