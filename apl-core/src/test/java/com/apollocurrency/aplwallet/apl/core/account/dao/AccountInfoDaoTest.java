/*
 * Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.core.account.dao;

import com.apollocurrency.aplwallet.apl.core.account.model.AccountInfo;
import com.apollocurrency.aplwallet.apl.core.app.Blockchain;
import com.apollocurrency.aplwallet.apl.core.app.BlockchainImpl;
import com.apollocurrency.aplwallet.apl.core.app.BlockchainProcessor;
import com.apollocurrency.aplwallet.apl.core.app.BlockchainProcessorImpl;
import com.apollocurrency.aplwallet.apl.core.chainid.BlockchainConfig;
import com.apollocurrency.aplwallet.apl.core.db.DatabaseManager;
import com.apollocurrency.aplwallet.apl.core.db.DerivedDbTablesRegistryImpl;
import com.apollocurrency.aplwallet.apl.core.db.fulltext.FullTextConfigImpl;
import com.apollocurrency.aplwallet.apl.core.db.fulltext.FullTextSearchEngine;
import com.apollocurrency.aplwallet.apl.core.db.fulltext.FullTextSearchService;
import com.apollocurrency.aplwallet.apl.data.AccountTestData;
import com.apollocurrency.aplwallet.apl.extension.DbExtension;
import com.apollocurrency.aplwallet.apl.testutil.DbUtils;
import com.apollocurrency.aplwallet.apl.util.injectable.PropertiesHolder;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.apollocurrency.aplwallet.apl.core.app.CollectionUtil.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@EnableWeld
class AccountInfoDaoTest {

    @RegisterExtension
    static DbExtension dbExtension = new DbExtension(Map.of("account_info", List.of("name", "description")));
    @Inject
    AccountInfoTable table;
    AccountTestData testData = new AccountTestData();
    private Blockchain blockchain = mock(BlockchainImpl.class);
    private BlockchainConfig blockchainConfig = mock(BlockchainConfig.class);
    private BlockchainProcessor blockchainProcessor = mock(BlockchainProcessor.class);
    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
        PropertiesHolder.class,
        FullTextConfigImpl.class, DerivedDbTablesRegistryImpl.class,
        AccountInfoTable.class
    )
        .addBeans(MockBean.of(dbExtension.getDatabaseManager(), DatabaseManager.class))
        .addBeans(MockBean.of(dbExtension.getDatabaseManager().getJdbi(), Jdbi.class))
        .addBeans(MockBean.of(blockchainConfig, BlockchainConfig.class))
        .addBeans(MockBean.of(blockchain, Blockchain.class, BlockchainImpl.class))
        .addBeans(MockBean.of(blockchainProcessor, BlockchainProcessor.class, BlockchainProcessorImpl.class))
        //.addBeans(MockBean.of(mock(FullTextConfig.class), FullTextConfig.class, FullTextConfigImpl.class))
        //.addBeans(MockBean.of(mock(DerivedTablesRegistry.class), DerivedTablesRegistry.class, DerivedDbTablesRegistryImpl.class))
        .addBeans(MockBean.of(dbExtension.getFtl(), FullTextSearchService.class))
        .addBeans(MockBean.of(dbExtension.getLuceneFullTextSearchEngine(), FullTextSearchEngine.class))
        .build();

    @Test
    void testSave_insert_new_entity() {//SQL MERGE -> INSERT
        AccountInfo previous = table.get(table.getDbKeyFactory().newKey(testData.newInfo));
        assertNull(previous);

        DbUtils.inTransaction(dbExtension, (con) -> table.insert(testData.newInfo));
        AccountInfo actual = table.get(table.getDbKeyFactory().newKey(testData.newInfo));

        assertNotNull(actual);
        assertTrue(actual.getDbId() != 0);
        assertEquals(testData.newInfo.getAccountId(), actual.getAccountId());
        assertEquals(testData.newInfo.getName(), actual.getName());
    }

    @Test
    void testSave_update_existing_entity() {//SQL MERGE -> UPDATE
        AccountInfo previous = table.get(table.getDbKeyFactory().newKey(testData.ACC_INFO_0));
        assertNotNull(previous);
        previous.setName("Ping-Pong " + previous.getName());

        DbUtils.inTransaction(dbExtension, (con) -> table.insert(previous));
        AccountInfo actual = table.get(table.getDbKeyFactory().newKey(previous));

        assertNotNull(actual);
        assertTrue(actual.getName().startsWith("Ping-Pong "));
        assertEquals(previous.getDescription(), actual.getDescription());
    }

    @Test
    void searchAccounts() {
        List<AccountInfo> result = toList(table.searchAccounts("CALIG*", 0, Integer.MAX_VALUE));
        assertEquals(List.of(testData.ACC_INFO_1, testData.ACC_INFO_4), result);
    }
}