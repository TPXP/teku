/*
 * Copyright Consensys Software Inc., 2022
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

package tech.pegasys.teku.test.acceptance;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.time.SystemTimeProvider;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.test.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.teku.test.acceptance.dsl.BesuNode;
import tech.pegasys.teku.test.acceptance.dsl.TekuBeaconNode;
import tech.pegasys.teku.test.acceptance.dsl.TekuNodeConfigBuilder;
import tech.pegasys.teku.test.acceptance.dsl.tools.deposits.ValidatorKeystores;

public class WebsocketsMergeTransitionAcceptanceTest extends AcceptanceTestBase {
  private static final String NETWORK_NAME = "swift";

  private final SystemTimeProvider timeProvider = new SystemTimeProvider();
  private BesuNode eth1Node;
  private TekuBeaconNode tekuNode;

  @BeforeEach
  void setup() throws Exception {
    final URL jwtFile = Resources.getResource("auth/ee-jwt-secret.hex");
    final int genesisTime = timeProvider.getTimeInSeconds().plus(10).intValue();
    eth1Node =
        createBesuNode(
            config ->
                config
                    .withMiningEnabled(true)
                    .withMergeSupport()
                    .withGenesisFile("besu/preMergeGenesis.json")
                    .withJwtTokenAuthorization(jwtFile));
    eth1Node.start();

    final int totalValidators = 4;
    final ValidatorKeystores validatorKeystores =
        createTekuDepositSender(NETWORK_NAME).sendValidatorDeposits(eth1Node, totalValidators);
    tekuNode =
        createTekuBeaconNode(
            configureTekuNode(genesisTime)
                .withDepositsFrom(eth1Node)
                .withStartupTargetPeerCount(0)
                .withValidatorProposerDefaultFeeRecipient(
                    "0xFE3B557E8Fb62b89F4916B721be55cEb828dBd73")
                .withExecutionEngine(eth1Node)
                .withJwtSecretFile(jwtFile)
                .withReadOnlyKeystorePath(validatorKeystores)
                .build());
    tekuNode.start();
  }

  @Test
  void shouldPassMergeTransitionUsingWebsocketsEngine() {
    tekuNode.waitForGenesis();
    tekuNode.waitForNonDefaultExecutionPayload();

    tekuNode.waitForNewFinalization();
  }

  private TekuNodeConfigBuilder configureTekuNode(final int genesisTime) throws IOException {
    return TekuNodeConfigBuilder.createBeaconNode()
        .withTotalTerminalDifficulty(10001)
        .withBellatrixEpoch(UInt64.ONE)
        .withGenesisTime(genesisTime);
  }
}
