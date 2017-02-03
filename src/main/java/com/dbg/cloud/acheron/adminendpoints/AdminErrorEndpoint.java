package com.dbg.cloud.acheron.adminendpoints;

import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;

public class AdminErrorEndpoint extends BasicErrorController {

    public AdminErrorEndpoint(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    @Override
    protected boolean isIncludeStackTrace(HttpServletRequest request, MediaType produces) {
        return false;
    }
}
