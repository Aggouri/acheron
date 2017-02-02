package com.dbg.cloud.acheron.adminendpoints;

/**
 * Marker interface for all admin endpoints. All {@link org.springframework.stereotype.Controller} beans implementing
 * this interface are automatically mapped to the admin child context and as a result end up on the admin port and
 * request context.
 */
public interface AdminEndpoint {
}
