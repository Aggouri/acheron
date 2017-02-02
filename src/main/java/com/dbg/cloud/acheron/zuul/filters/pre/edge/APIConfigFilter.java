package com.dbg.cloud.acheron.zuul.filters.pre.edge;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.pluginconfig.PluginConfig;
import com.dbg.cloud.acheron.pluginconfig.service.PluginConfigService;
import com.dbg.cloud.acheron.zuul.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class APIConfigFilter extends PreFilter {

    private final PluginConfigService configService;

    @Override
    public int filterOrder() {
        return 14;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();
        final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);
        final String httpMethod = context.getRequest().getMethod();

        // simulate realm concept
        // TODO Come up with multi-realm concept
        context.set(AcheronRequestContextKeys.REALM_ID, "realm1");

        final Collection<PluginConfig> routeConfigurations = configService.getPluginConfigsOfRoute(routeId);
        final Collection<PluginConfig> enabledPlugins = routeConfigurations.stream().filter(
                pluginConfig -> (shouldPluginConfigBeIncluded(pluginConfig, httpMethod)))
                .collect(Collectors.toList());

        enabledPlugins.forEach(pluginConfig -> {
            context.set("plugins." + pluginConfig.getName() + ".enabled");
            context.set("plugins." + pluginConfig.getName() + ".config", pluginConfig.getConfig());
        });

        return null;
    }

    private boolean shouldPluginConfigBeIncluded(final PluginConfig pluginConfig, final String httpMethod) {
        return pluginConfig.isEnabled() && pluginConfig.getConsumerId() == null &&
                methodMatchesMethods(httpMethod, pluginConfig.getHttpMethods());
    }

    private boolean methodMatchesMethods(final String method, final @NonNull Set<String> methods) {
        return methods.contains("*") || methods.contains(method);
    }
}
