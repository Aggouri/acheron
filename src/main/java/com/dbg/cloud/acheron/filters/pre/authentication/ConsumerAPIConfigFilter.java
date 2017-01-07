package com.dbg.cloud.acheron.filters.pre.authentication;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filter that loads plugin configuration related to the consumer. Can only run after the Consumer identity has been
 * established.
 */
@AllArgsConstructor
public class ConsumerAPIConfigFilter extends PreFilter {

    private final PluginConfigStore configStore;

    @Override
    public int filterOrder() {
        return 39;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();

        final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);
        final String consumerId = getConsumerId();
        final String httpMethod = context.getRequest().getMethod();

        // Get configuration for consumer (this route-specific or generic)
        List<PluginConfig> routeConfigurations = configStore.findByConsumer(UUID.fromString(consumerId));
        List<PluginConfig> enabledPlugins = routeConfigurations.stream().filter(
                pluginConfig -> (shouldPluginConfigBeIncluded(pluginConfig, routeId, httpMethod)))
                .collect(Collectors.toList());

        enabledPlugins.forEach(pluginConfig -> {
            context.set("plugins." + pluginConfig.getName() + ".enabled");
        });

        return null;
    }

    @Override
    public boolean shouldFilter() {
        return getConsumerId() != null;
    }

    private boolean shouldPluginConfigBeIncluded(final PluginConfig pluginConfig, final String routeId,
                                                 final String httpMethod) {
        return pluginConfig.isEnabled() &&
                (pluginConfig.getRouteId() == null || routeId.equals(pluginConfig.getRouteId())) &&
                methodMatchesMethods(httpMethod, pluginConfig.getHttpMethods());
    }

    private boolean methodMatchesMethods(final String method, final @NonNull Set<String> methods) {
        return methods.contains("*") || methods.contains(method);
    }
}
