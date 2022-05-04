/*
 * Copyright 2022 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.beaconrestapi;

import tech.pegasys.teku.api.ChainDataProvider;
import tech.pegasys.teku.core.ChainBuilder;
import tech.pegasys.teku.spec.SpecMilestone;
import tech.pegasys.teku.spec.TestSpecFactory;
import tech.pegasys.teku.statetransition.validatorcache.ActiveValidatorCache;
import tech.pegasys.teku.statetransition.validatorcache.ActiveValidatorChannel;
import tech.pegasys.teku.storage.client.ChainUpdater;
import tech.pegasys.teku.storage.client.CombinedChainDataClient;
import tech.pegasys.teku.storage.client.RecentChainData;
import tech.pegasys.teku.storage.server.StateStorageMode;
import tech.pegasys.teku.storage.storageSystem.InMemoryStorageSystemBuilder;
import tech.pegasys.teku.storage.storageSystem.StorageSystem;

public class AbstractMigratedBeaconHandlerWithChainDataProviderTest
    extends AbstractMigratedBeaconHandlerTest {
  protected ChainBuilder chainBuilder;
  protected ChainUpdater chainUpdater;

  protected ActiveValidatorChannel activeValidatorChannel;
  protected StorageSystem storageSystem =
      InMemoryStorageSystemBuilder.buildDefault(StateStorageMode.ARCHIVE);
  protected CombinedChainDataClient combinedChainDataClient;
  protected RecentChainData recentChainData = storageSystem.recentChainData();

  public void initialise(final SpecMilestone specMilestone) {
    setupStorage(StateStorageMode.ARCHIVE, specMilestone, false);
  }

  public void genesis() {
    storageSystem.chainUpdater().initializeGenesis();
  }

  private void setupStorage(
      final StateStorageMode storageMode,
      final SpecMilestone specMilestone,
      final boolean storeNonCanonicalBlocks) {
    this.spec = TestSpecFactory.createMinimal(specMilestone);
    this.storageSystem =
        InMemoryStorageSystemBuilder.create()
            .specProvider(spec)
            .storageMode(storageMode)
            .storeNonCanonicalBlocks(storeNonCanonicalBlocks)
            .build();
    activeValidatorChannel = new ActiveValidatorCache(spec, 10);
    recentChainData = storageSystem.recentChainData();
    combinedChainDataClient = storageSystem.combinedChainDataClient();

    chainBuilder = storageSystem.chainBuilder();
    chainUpdater = storageSystem.chainUpdater();
    chainDataProvider = new ChainDataProvider(spec, recentChainData, combinedChainDataClient);
  }
}
