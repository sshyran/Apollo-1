package com.apollocurrency.aplwallet.apl.core.rest;

import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.PlatformSpec;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
@Getter
@ToString
public class PlatformSpecs {
    private List<PlatformSpec> specList = new ArrayList<>();

}
