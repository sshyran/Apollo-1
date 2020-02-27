/*
 * Copyright © 2018-2019 Apollo Foundation
 */
package com.apollocurrency.aplwallet.apl.core.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for implemented endpoints of new API.
 * Should be removed along with ApiSplitFilter after
 * @author alukin@gmail.com
 */
public class NewApiRegistry {
    private static Map<String,String> apis = new HashMap<>();
    static{
        apis.put("getServerInfo", "/rest/server-info");
        apis.put("importKeyStore", "/rest/keyStore/upload");
        apis.put("exportKeyStore", "/rest/keyStore/download");
        apis.put("getAccountInfo", "/rest/keyStore/accountInfo");

        apis.put("getEthWalletAmount", "/rest/wallet/eth");
        apis.put("getEthWalletTransfer", "/rest/wallet/eth/transfer");


        apis.put("getDexHistory", "/rest/dex/history");
        apis.put("getDexOffers", "/rest/dex/offers");
        apis.put("getDexOrders", "/rest/dex/order");
        apis.put("getDexWidthraw", "/rest/dex/widthraw");

        apis.put("getMyInfo", "/rest/networking/peer/mypeerinfo"); //GET
        apis.put("getPeer", "/rest/networking/peer"); //GET
        apis.put("addPeer", "/rest/networking/peer"); //POST
        apis.put("getPeers", "/rest/networking/peer/all"); //GET
        apis.put("getInboundPeers",  "/rest/networking/peer/inbound"); //GET
        apis.put("blacklistPeer", "/rest/networking/peer/blacklist"); //POST
        apis.put("blacklistAPIProxyPeer", "/rest/networking/peer/proxyblacklist"); //POST
        apis.put("setAPIProxyPeer", "/rest/networking/peer/setproxy"); //POST

        apis.put("getAccount", "/rest/accounts/account"); //GET
        apis.put("generateAccount", "/rest/accounts/account"); //POST
        apis.put("enable2FA", "/rest/accounts/enable2fa"); //POST
        apis.put("disable2FA", "/rest/accounts/disable2fa"); //POST
        apis.put("confirm2FA", "/rest/accounts/confirm2fa"); //POST
        apis.put("deleteKey", "/rest/accounts/delete-key"); //POST
        apis.put("exportKey", "/rest/accounts/export-key"); //POST
        apis.put("getAccountAssetCount", "/rest/accounts/asset-count"); //GET
        apis.put("getAccountAssets", "/rest/accounts/assets"); //GET
        apis.put("getAccountBlockCount", "/rest/accounts/block-count"); //GET
        apis.put("getAccountBlockIds", "/rest/accounts/block-ids"); //GET
        apis.put("getAccountBlocks", "/rest/accounts/blocks"); //GET
        apis.put("getAccountCurrencyCount", "/rest/accounts/currency-count"); //GET
        apis.put("getAccountCurrencies", "/rest/accounts/currencies"); //GET
        apis.put("getAccountCurrentAskOrderIds", "/rest/accounts/current-ask-order-ids"); //GET



        //TODO: add new implemented endpoints
    }
    public static String getRestPath(String rqType) {
        if(rqType==null || rqType.isEmpty()){
            return null;
        }
        return apis.get(rqType);
    }

}