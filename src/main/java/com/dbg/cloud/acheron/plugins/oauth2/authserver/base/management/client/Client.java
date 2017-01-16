package com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client;

import java.util.Set;

public interface Client {

    String getClientName();

    String getClientId();

    Set<String> getRedirectURIs();

    Set<String> getGrantTypes();

    Set<String> getResponseTypes();

    String getScope();

    String getOwner();

    String getPolicyURI();

    String getTosURI();

    String getClientURI();

    String getLogoURI();

    String getContacts();

    Boolean getIsPublic();
}
