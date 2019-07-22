/*
 * Copyright © 2018 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.core.app;

import com.apollocurrency.aplwallet.apl.core.app.observer.events.ShardPresentEvent;
import com.apollocurrency.aplwallet.apl.core.app.observer.events.ShardPresentEventType;
import com.apollocurrency.aplwallet.apl.core.chainid.BlockchainConfigUpdater;
import com.apollocurrency.aplwallet.apl.core.db.DatabaseManager;
import com.apollocurrency.aplwallet.apl.core.db.DerivedTablesRegistry;
import com.apollocurrency.aplwallet.apl.core.db.TransactionalDataSource;
import com.apollocurrency.aplwallet.apl.core.db.derived.DerivedTableInterface;
import com.apollocurrency.aplwallet.apl.core.shard.ShardImporter;
import com.apollocurrency.aplwallet.apl.core.shard.ShardPresentData;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Interface for shard data downloading management. It does following:
 * In case empty database (node is started from the scratch) it will do:
 * - connect to peers and check if sharding is present in current network
 * - gather statistics info about shard hashes/data from peers
 *
 * If shard is not present it starts 'old' process importing Genesis data
 */
@Slf4j
@Singleton
public class ShardDownloadPresenceObserver {

    private final DatabaseManager databaseManager;
    private final Blockchain blockchain;
    private final BlockchainProcessor blockchainProcessor;
    private final DerivedTablesRegistry derivedTablesRegistry;
    private final ShardImporter shardImporter;
    private final BlockchainConfigUpdater blockchainConfigUpdater;
    private final GenesisImporter genesisImporter;

    @Inject
    public ShardDownloadPresenceObserver(DatabaseManager databaseManager, BlockchainProcessor blockchainProcessor,
                                         Blockchain blockchain, DerivedTablesRegistry derivedTablesRegistry,
                                         ShardImporter shardImporter, BlockchainConfigUpdater blockchainConfigUpdater,
                                         GenesisImporter genesisImporter) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager is NULL");
        this.blockchainProcessor = Objects.requireNonNull(blockchainProcessor, "blockchainProcessor is NULL");
        this.derivedTablesRegistry = Objects.requireNonNull(derivedTablesRegistry, "derivedTablesRegistry is NULL");
        this.blockchain = Objects.requireNonNull(blockchain, "blockchain is NULL");
        this.shardImporter = Objects.requireNonNull(shardImporter, "shardImporter is NULL");
        this.blockchainConfigUpdater = Objects.requireNonNull(blockchainConfigUpdater, "blockchainConfigUpdater is NULL");
        this.genesisImporter = Objects.requireNonNull(genesisImporter, "genesisImporter is NULL");
    }

    /**
     * Method is called when node knows it's int sharded network and shard ZIP information has been downloaded by transport subsystem.
     *
     * @param shardPresentData shard present data contains downloaded ZIP name
     */
    public void onShardPresent(@ObservesAsync @ShardPresentEvent(ShardPresentEventType.SHARD_PRESENT) ShardPresentData shardPresentData) {
        String fileId = shardPresentData.getFileIdValue();
        try {
            shardImporter.importShard(fileId, List.of());
        } catch (Exception e) {
            log.error("Error on Shard # {}. Zip/CSV importing...\nerror: {}", fileId, e);
            log.error("Node has encountered serious error and import CSV shard data. " +
                    "Somethings wrong with processing fileId =\n'{}'\n >>> FALL BACK to Genesis importing....", fileId);
            log.error("Please delete database files and try to run with --no-shards-import command line option");
            System.exit(-1); // temporary solution
/*
            // truncate partial data potentially imported into database
            this.blockchainProcessor.fullReset();
            // fall back to importing Genesis and starting from beginning
            onNoShardPresent(shardPresentData);
            return;
*/
        }
        log.info("SNAPSHOT block should be READY in database...");
        blockchainProcessor.updateInitialSnapshotBlock();
        Block lastBlock = blockchain.findLastBlock();
        log.debug("SNAPSHOT Last block height: " + lastBlock.getHeight());
        blockchainConfigUpdater.updateToLatestConfig();
        blockchainProcessor.setGetMoreBlocks(true); // turn ON blockchain downloading
        log.info("onShardPresent() finished Last block height: " + lastBlock.getHeight());
    }

    /**
     * Process event when no sharding is present in network and we should go 'old scenario'
     *
     * @param shardPresentData not used actually
     */
    public void onNoShardPresent(@Observes @ShardPresentEvent(ShardPresentEventType.NO_SHARD) ShardPresentData shardPresentData) {
        // start adding old Genesis Data
            try {
                log.info("Genesis block not in database, starting from scratch");
                TransactionalDataSource dataSource = databaseManager.getDataSource();
//        Connection con = dataSource.begin();
                try (Connection con = dataSource.begin()) {
                    Block genesisBlock = genesisImporter.newGenesisBlock();
                    addBlock(dataSource, genesisBlock);
                    long initialBlockId = genesisBlock.getId();
                    log.debug("Generated Genesis block with Id = {}", initialBlockId);
                    genesisImporter.importGenesisJson(false);
                    for (DerivedTableInterface table : derivedTablesRegistry.getDerivedTables()) {
                        table.createSearchIndex(con);
                    }
                    blockchain.commit(genesisBlock);
                    dataSource.commit();
                    log.debug("Saved Genesis block = {}", genesisBlock);
                } catch (SQLException e) {
                    dataSource.rollback();
                    log.info(e.getMessage());
                    throw new RuntimeException(e.toString(), e);
                }
                // set to start work block download thread (starting from Genesis block here)
                log.debug("Before updating BlockchainProcessor from Genesis and RESUME block downloading...");
                blockchainProcessor.updateInitialBlockId();
                blockchainProcessor.resumeBlockchainDownloading(); // IMPORTANT CALL !!!

            } catch (Exception e) {
                log.error(e.toString(), e);
            }
    }

    private void addBlock(TransactionalDataSource dataSource, Block block) {
        try (Connection con = dataSource.getConnection()) {
            blockchain.saveBlock(con, block);
            blockchain.setLastBlock(block);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }


}
