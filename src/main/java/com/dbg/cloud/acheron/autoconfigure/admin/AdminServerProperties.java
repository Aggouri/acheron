package com.dbg.cloud.acheron.autoconfigure.admin;

import org.springframework.boot.autoconfigure.security.SecurityPrerequisite;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "admin", ignoreUnknownFields = true)
public class AdminServerProperties implements SecurityPrerequisite {

    /**
     * Order applied to the WebSecurityConfigurerAdapter that is used to configure basic
     * authentication for admin endpoints. If you want to add your own authentication
     * for all or some of those endpoints the best thing to do is add your own
     * WebSecurityConfigurerAdapter with lower order, for instance by using
     * {@code ACCESS_OVERRIDE_ORDER}.
     */
    public static final int BASIC_AUTH_ORDER = SecurityProperties.BASIC_AUTH_ORDER - 5;

    /**
     * Order before the basic authentication access control provided automatically for the
     * admin endpoints. This is a useful place to put user-defined access rules if
     * you want to override the default access rules for the admin endpoints. If you
     * want to keep the default rules for admin endpoints but want to override the
     * security for the rest of the application, use
     * {@code SecurityProperties.ACCESS_OVERRIDE_ORDER} instead.
     */
    public static final int ACCESS_OVERRIDE_ORDER = BASIC_AUTH_ORDER - 1;

    /**
     * Admin endpoint HTTP port. Use the same port as the application by default.
     */
    private Integer port;

    @NestedConfigurationProperty
    private Ssl ssl;

    /**
     * Network address that the admin endpoints should bind to.
     */
    private InetAddress address;

    /**
     * Admin endpoint context-path.
     */
    @NotNull
    private String contextPath = "";

    /**
     * Add the "X-Application-Context" HTTP header in each response.
     */
    private boolean addApplicationContextHeader = true;

    private final Security security = new Security();

    /**
     * Returns the admin port or {@code null} if the
     * {@link ServerProperties#getPort() server port} should be used.
     *
     * @return the port
     * @see #setPort(Integer)
     */
    public Integer getPort() {
        return this.port;
    }

    /**
     * Sets the port of the admin server, use {@code null} if the
     * {@link ServerProperties#getPort() server port} should be used. To disable use 0.
     *
     * @param port the port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    public Ssl getSsl() {
        return this.ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Return the context path with no trailing slash (i.e. the '/' root context is
     * represented as the empty string).
     *
     * @return the context path (no trailing slash)
     */
    public String getContextPath() {
        return this.contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = cleanContextPath(contextPath);
    }

    private String cleanContextPath(String contextPath) {
        if (StringUtils.hasText(contextPath) && contextPath.endsWith("/")) {
            return contextPath.substring(0, contextPath.length() - 1);
        }
        return contextPath;
    }

    public Security getSecurity() {
        return this.security;
    }

    public boolean getAddApplicationContextHeader() {
        return this.addApplicationContextHeader;
    }

    public void setAddApplicationContextHeader(boolean addApplicationContextHeader) {
        this.addApplicationContextHeader = addApplicationContextHeader;
    }

    /**
     * Security configuration.
     */
    public static class Security {

        /**
         * Enable security.
         */
        private boolean enabled = true;

        /**
         * Comma-separated list of roles that can access the admin endpoint.
         */
        private List<String> roles = Arrays.asList("ADMIN");

        /**
         * Session creating policy for security use (always, never, if_required,
         * stateless).
         */
        private SessionCreationPolicy sessions = SessionCreationPolicy.STATELESS;

        public SessionCreationPolicy getSessions() {
            return this.sessions;
        }

        public void setSessions(SessionCreationPolicy sessions) {
            this.sessions = sessions;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        @Deprecated
        public void setRole(String role) {
            this.roles = Arrays.asList(role);
        }

        public List<String> getRoles() {
            return this.roles;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

    public enum SessionCreationPolicy {

        /**
         * Always create an {@link HttpSession}.
         */
        ALWAYS,

        /**
         * Never create an {@link HttpSession}, but use any {@link HttpSession} that
         * already exists.
         */
        NEVER,

        /**
         * Only create an {@link HttpSession} if required.
         */
        IF_REQUIRED,

        /**
         * Never create an {@link HttpSession}.
         */
        STATELESS

    }

}
