package com.dbg.cloud.acheron;

import com.dbg.cloud.acheron.consumers.store.ConsumerStore;
import com.dbg.cloud.acheron.pluginconfig.store.PluginConfigStore;
import com.dbg.cloud.acheron.routing.store.RouteStore;
import com.dbg.cloud.acheron.plugins.apikey.store.APIKeyStore;
import com.dbg.cloud.acheron.plugins.oauth2.store.OAuth2Store;
import com.dbg.cloud.acheron.plugins.ratelimiting.store.RateLimitStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.cloud.bus.enabled=false",
        "store.cassandra.enabled=false",
        "store.cassandra.routing=false",
        "store.cassandra.plugins=false",
        "store.cassandra.consumers=false"})
public class AcheronApplicationTests {

    @MockBean
    private ConsumerStore consumerStore;

    @MockBean
    private PluginConfigStore pluginConfigStore;

    @MockBean
    private RouteStore routeStore;

    @MockBean
    private APIKeyStore apiKeyStore;

    @MockBean
    private OAuth2Store oAuth2Store;

    @MockBean
    private RateLimitStore rateLimitStore;


    @Test
    public void contextLoads() {
    }
}
