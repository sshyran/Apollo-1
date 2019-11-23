package com.apollocurrency.aplwallet.apl.exchange.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.apollocurrency.aplwallet.apl.core.config.DaoConfig;
import com.apollocurrency.aplwallet.apl.core.db.DatabaseManager;
import com.apollocurrency.aplwallet.apl.core.db.cdi.transaction.JdbiHandleFactory;
import com.apollocurrency.aplwallet.apl.data.DexTestData;
import com.apollocurrency.aplwallet.apl.exchange.model.ExchangeContract;
import com.apollocurrency.aplwallet.apl.extension.DbExtension;
import com.apollocurrency.aplwallet.apl.testutil.WeldUtils;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import javax.inject.Inject;

@EnableWeld
public class DexContractDaoTest {
    @RegisterExtension
    DbExtension extension = new DbExtension();

    @WeldSetup
    WeldInitiator weld =  WeldUtils.from(List.of(DexContractDao.class, DaoConfig.class), List.of())
            .addBeans(MockBean.of(extension.getDatabaseManager().getJdbi(), Jdbi.class))
            .addBeans(MockBean.of(extension.getDatabaseManager().getJdbiHandleFactory(), JdbiHandleFactory.class))
            .addBeans(MockBean.of(extension.getDatabaseManager(), DatabaseManager.class))
            .build();

    @Inject
    DexContractDao dao;
    DexTestData td = new DexTestData();

    @Test
    void testGetContractsForAccount() {
        List<ExchangeContract> contract = dao.getAllForAccountOrder(td.EXCHANGE_CONTRACT_2.getSender(), td.EXCHANGE_CONTRACT_2.getCounterOrderId(), 0, 2);

        assertEquals(List.of(td.EXCHANGE_CONTRACT_2), contract);
    }
}
