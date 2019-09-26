/*
 * Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.data;

import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexContractAttachment;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.exchange.model.DexCurrencies;
import com.apollocurrency.aplwallet.apl.exchange.model.DexOrder;
import com.apollocurrency.aplwallet.apl.exchange.model.DexTradeEntry;
import com.apollocurrency.aplwallet.apl.exchange.model.ExchangeContract;
import com.apollocurrency.aplwallet.apl.exchange.model.ExchangeContractStatus;
import com.apollocurrency.aplwallet.apl.exchange.model.OrderStatus;
import com.apollocurrency.aplwallet.apl.exchange.model.OrderType;
import com.apollocurrency.aplwallet.apl.util.Constants;

import java.math.BigDecimal;

public class DexTestData {
    public final long ALICE = 100;
    public final long BOB = 200;
    // type(Buy/Sell currency (ETH/PAX) account (Alice/BOB)
    public final DexOrder ORDER_BEA_1 = new DexOrder(1000L, 1L, OrderType.BUY, 100L, DexCurrencies.APL, 500000L, DexCurrencies.ETH, BigDecimal.valueOf(0.001), 6000, OrderStatus.CLOSED, 100, "0x602242c68640e754677b683e20a2740f8f95f7d3", "APL-K78W-Z7LR-TPJY-73HZK");
    public final DexOrder ORDER_SPA_2 = new DexOrder(1010L, 2L, OrderType.SELL, 100L, DexCurrencies.APL, 200000L, DexCurrencies.PAX, BigDecimal.valueOf(0.16), 6500, OrderStatus.CANCEL, 110, "APL-K78W-Z7LR-TPJY-73HZK", "0x602242c68640e754677b683e20a2740f8f95f7d3");
    public final DexOrder ORDER_BPB_1 = new DexOrder(1020L, 3L, OrderType.BUY, 200L, DexCurrencies.APL, 100000L, DexCurrencies.PAX, BigDecimal.valueOf(0.15), 7000, OrderStatus.OPEN, 121, "0x777BE94ea170AfD894Dd58e9634E442F6C5602EF", "APL-T69E-CTDG-8TYM-DKB5H");
    public final DexOrder ORDER_SEA_3 = new DexOrder(1030L, 4L, OrderType.SELL, 100L, DexCurrencies.APL, 400000L, DexCurrencies.ETH, BigDecimal.valueOf(0.001), 8000, OrderStatus.WAITING_APPROVAL, 121, "APL-K78W-Z7LR-TPJY-73HZK", "0x602242c68640e754677b683e20a2740f8f95f7d3");
    public final DexOrder ORDER_BEA_4 = new DexOrder(1040L, 5L, OrderType.BUY, 100L, DexCurrencies.APL, 600000L, DexCurrencies.ETH, BigDecimal.valueOf(0.001), 11000, OrderStatus.OPEN, 122, "0x602242c68640e754677b683e20a2740f8f95f7d3", "APL-K78W-Z7LR-TPJY-73HZK");

    public static final long EXCHANGE_CONTRACT_ID_1       =  -3625894990594689368L ;
    public static final long EXCHANGE_CONTRACT_ID_2       =  -7277152511870517934L ;
    public static final long EXCHANGE_CONTRACT_ID_3       =  8455581613897449491L ;

    public final ExchangeContract EXCHANGE_CONTRACT_1;
    public final ExchangeContract EXCHANGE_CONTRACT_2;
    public final ExchangeContract EXCHANGE_CONTRACT_3;

    public final ExchangeContract NEW_EXCHANGE_CONTRACT_4;
    public final long NEW_EXCHANGE_CONTRACT_ID = -762612439991997299L;
    public final long NEW_EXCHANGE_CONTRACT_SENDER_ID = -382612439991997299L;
    public final long NEW_EXCHANGE_CONTRACT_RECIPIENT_ID = 4582612439991997299L;

    // contract attachment
    private String hash = "f0af17449a83681de22db7ce16672f16f37131bec0022371d4ace5d1854301e0";
    private String encryptedSecret = "ce6b20ee7f7797e102f68d15099e7d5b0e8d4c50f98a7865ea168717539ec3aace6b20ee7f7797e102f68d15099e7d5b0e8d4c50f98a7865ea168717539ec3aa";
    public final DexContractAttachment DEX_CONTRACT_ATTACHMENT_1;
    public final DexContractAttachment DEX_CONTRACT_ATTACHMENT_2;
    public final DexContractAttachment DEX_CONTRACT_ATTACHMENT_3;
    public final DexContractAttachment NEW_DEX_CONTRACT_ATTACHMENT_4;

    public static final long DEX_TRADE_ID_1       =  -3625894990594689368L ;

    public final DexTradeEntry NEW_TRADE_ENTRY_1;

    public DexTestData() {

        DEX_CONTRACT_ATTACHMENT_1 = new DexContractAttachment(-5227805726286506078L, -7138882269097972721L,
                Convert.parseHexString("f41a9d03745d78c8efd682b4f6030fd70623e5c38ae2115d53f2c94f483aa121"), null, "0x73949de85a63ed24457fc4188c8876726024a3f67fa673389a7aae47698e61bd",
                Convert.parseHexString("b4f38c90ab6f36fc76013a7a69152186e2c44ef73d188a041770c253d6ccd1b88e24f37ab3c0bfd77fc74a4600c4090aea1dc1a297a2aa3400a330cb6f670fec"),
                ExchangeContractStatus.STEP_1, Constants.DEX_CONTRACT_TIME_WAITING_TO_REPLY);
        DEX_CONTRACT_ATTACHMENT_2 = new DexContractAttachment(4066034979755747272L, 6794334481055229134L,
                Convert.parseHexString("8e0f875179dd784241babdc56e1380370620db1c8aa1b7f765e2b98cd3fc2840"), "12380311258696115355", "0xe50bd6b4c62d8fb167de66c11a7a57cbcc97a2e945ddd3829d7cf0f09fda7b14",
                Convert.parseHexString("e670c46452e18fe2224edf5fba888affef6060e0efeeb10862bcfdebfcfcf997dc37443b1ff44c79977f484e4b4e2e94404620145ebeee5bce7a2f609b453e13"),
                ExchangeContractStatus.STEP_2, Constants.DEX_CONTRACT_TIME_WAITING_TO_REPLY);
        DEX_CONTRACT_ATTACHMENT_3 = new DexContractAttachment(5339180579805180746L, -5842203753269117069L,
                Convert.parseHexString("509520c8d27b08b9208b38f6ab1735c043263c18d2579a44f2210135ca92b480"), "0x8540339763b19265f394140544fe060711b1e0623860d8b99e21ffc769574f50", "4340657620930323843",
                Convert.parseHexString("d6e6c72256548595c331c66d0d3fb5b1141b26e2d15946092acb3e3e46b781f7f52148408a9f0d845333cccab9c822f13149eae2ab5b963c921e4a7e97dabd7f"),
                ExchangeContractStatus.STEP_2, Constants.DEX_CONTRACT_TIME_WAITING_TO_REPLY);

        EXCHANGE_CONTRACT_1 = new ExchangeContract(10L, EXCHANGE_CONTRACT_ID_1, -582612439131997299L, -582612439131997299L, 53499864, DEX_CONTRACT_ATTACHMENT_1, 100);
        EXCHANGE_CONTRACT_2 = new ExchangeContract(20L, EXCHANGE_CONTRACT_ID_2, 7477442401604846627L, 7477442401604846627L, 53499868, DEX_CONTRACT_ATTACHMENT_2, 200);
        EXCHANGE_CONTRACT_3 = new ExchangeContract(30L, EXCHANGE_CONTRACT_ID_3, 7477442401604846627L, -582612439131997299L, 53499882, DEX_CONTRACT_ATTACHMENT_3, 300);

        NEW_DEX_CONTRACT_ATTACHMENT_4 = new DexContractAttachment(5339180579805180746L, -5842203753269117069L,
                Convert.parseHexString("509520c8d27b08b9208b38f6ab1735c043263c18d2579a44f2210135ca92b480"), "0x8540339763b19265f394140544fe060711b1e0623860d8b99e21ffc769574f50", "4340657620930323843",
                Convert.parseHexString("d6e6c72256548595c331c66d0d3fb5b1141b26e2d15946092acb3e3e46b781f7f52148408a9f0d845333cccab9c822f13149eae2ab5b963c921e4a7e97dabd7f"),
                ExchangeContractStatus.STEP_2, Constants.DEX_CONTRACT_TIME_WAITING_TO_REPLY);
        NEW_EXCHANGE_CONTRACT_4 = new ExchangeContract(NEW_EXCHANGE_CONTRACT_ID, NEW_EXCHANGE_CONTRACT_SENDER_ID, NEW_EXCHANGE_CONTRACT_RECIPIENT_ID, 53499882, NEW_DEX_CONTRACT_ATTACHMENT_4);

        NEW_TRADE_ENTRY_1 = new DexTradeEntry(null, null);
    }
}
