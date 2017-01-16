package com.dbg.cloud.acheron.plugins.oauth2.authserver.hydra.management.client.creation;

import com.dbg.cloud.acheron.plugins.oauth2.authserver.base.management.client.creation.ClientCreationResult;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@AllArgsConstructor
@ToString
final class HydraClientCreationResult implements ClientCreationResult {

    @Delegate
    private final JSONResponse jsonResponse;
}
