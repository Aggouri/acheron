package com.dbg.cloud.acheron.filters.pre.edge;

import com.dbg.cloud.acheron.AcheronRequestContextKeys;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfig;
import com.dbg.cloud.acheron.config.store.plugins.PluginConfigStore;
import com.dbg.cloud.acheron.filters.pre.PreFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class APIConfigFilter extends PreFilter {

    private final PluginConfigStore configStore;

    @Override
    public int filterOrder() {
        return 13;
    }

    @Override
    public Object run() {
        final RequestContext context = RequestContext.getCurrentContext();

        final String routeId = (String) context.get(AcheronRequestContextKeys.ROUTE_ID);

        // simulate realm concept
        // TODO Come up with multi-realm concept
        context.set(AcheronRequestContextKeys.REALM_ID, "realm1");

        List<PluginConfig> routeConfigurations = configStore.findByRoute(routeId);
        List<PluginConfig> enabledPlugins = routeConfigurations.stream().filter(
                pluginConfig -> (pluginConfig.isEnabled() && pluginConfig.getConsumerId() == null))
                .collect(Collectors.toList());

        enabledPlugins.forEach(pluginConfig -> {
            context.set("plugins." + pluginConfig.getName() + ".enabled");
        });

        return null;
    }
}
