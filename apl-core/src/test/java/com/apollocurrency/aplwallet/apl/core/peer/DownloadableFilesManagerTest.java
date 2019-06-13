package com.apollocurrency.aplwallet.apl.core.peer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import java.nio.file.Path;

import com.apollocurrency.aplwallet.api.p2p.FileDownloadInfo;
import com.apollocurrency.aplwallet.apl.core.chainid.ChainsConfigHolder;
import com.apollocurrency.aplwallet.apl.core.shard.ShardNameHelper;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.testutil.ResourceFileLoader;
import com.apollocurrency.aplwallet.apl.util.Constants;
import com.apollocurrency.aplwallet.apl.util.Zip;
import com.apollocurrency.aplwallet.apl.util.ZipImpl;
import com.apollocurrency.aplwallet.apl.util.env.config.Chain;
import com.apollocurrency.aplwallet.apl.util.env.dirprovider.DirProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;

@EnableWeld
class DownloadableFilesManagerTest {
    private static final Logger log = getLogger(DownloadableFilesManagerTest.class);
    private Path csvResourcesPath = new ResourceFileLoader().getResourcePath().toAbsolutePath();
    private DirProvider dirProvider = mock(DirProvider.class);
    private ChainsConfigHolder chainCoinfig;
    private Chain chain;
    private final UUID chainId=UUID.fromString("b5d7b697-f359-4ce5-a619-fa34b6fb01a5");    
    {
        doReturn(csvResourcesPath).when(dirProvider).getDataExportDir();
        chain = mock(Chain.class);
        when(chain.getChainId()).thenReturn(chainId);
        chainCoinfig = mock(ChainsConfigHolder.class);
        when(chainCoinfig.getActiveChain()).thenReturn(chain);        
    }

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
               ShardNameHelper.class, 
               DownloadableFilesManager.class)
            .addBeans(MockBean.of(dirProvider, DirProvider.class))
            .addBeans(MockBean.of(chainCoinfig, ChainsConfigHolder.class))
            .build();
    String fileBaseDir =System.getProperty("java.io.tmpdir")+"/"+Constants.APPLICATION;
    String zipFileName = "apl-blockchain-arch-1.zip";

    @Inject
    private DownloadableFilesManager filesManager;
    
    private String createTestZip() throws IOException{
        int n_lines=1000;
        
        File tmpDir = new File(fileBaseDir);
        File wDir = new File(tmpDir.getAbsolutePath()+"/"+"apl-test-zip");
        if(wDir.exists()){
           FileUtils.deleteDirectory(wDir);
        }
        wDir.mkdirs();
        for(int i=0; i<10; i++){
          String fn="test_file_"+i;  
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(wDir.getAbsolutePath()+"/"+fn))) {
                for(int j=0; j<n_lines; j++){
                    writer.write("line "+j);
                } }
        }
        Zip zip = new ZipImpl();
        zip.compress(fileBaseDir+"/"+zipFileName, wDir.getAbsolutePath(), Long.MIN_VALUE, null,false);
        return wDir.getAbsolutePath();
    }
    
    @Test
    void getFileDownloadInfo() {
        String tdir=null;
        FileDownloadInfo fi=null;
        String fileId = "debug::"+zipFileName;
        try {
            // create ZIP in temp folder for unit test
            tdir = createTestZip();
            fi = filesManager.getFileDownloadInfo(fileId);
            FileUtils.deleteDirectory(new File(tdir));
        } catch (IOException ex) {
            fail("Can not create test files");
        }
        assertNotNull(fi);
        log.debug("File download Info = {}", fi);
        assertEquals(
                "f3b51cb318c7de39c345ba6344f2bb0068a2627e92a8d6466a7a98bf3fd3e1a2",
                fi.fileInfo.hash);
        log.debug("Parsed bytes from string = {}", Convert.parseHexString(fi.fileInfo.hash) );
        assertEquals(fileId, fi.fileInfo.fileId);
        File f = new File(fileBaseDir+"/"+zipFileName);
        f.delete();        
    }

    @Test
    void getMissingResource() {
        String zipFileName = "MISSING-archive.zip";

        FileDownloadInfo fi = filesManager.getFileDownloadInfo(zipFileName);
        assertEquals(fi.fileInfo.isPresent,false);
    }

    @Test
    void mapFileIdToLocalPath() {
        // parse real/existing shard name + ID
        Path pathToShardArchive = filesManager.mapFileIdToLocalPath("shard::1");
        assertNotNull(pathToShardArchive);
        assertEquals("apl-blockchain-shard-1-chain-b5d7b697-f359-4ce5-a619-fa34b6fb01a5.zip", pathToShardArchive.getFileName().toString());
        
        pathToShardArchive = filesManager.mapFileIdToLocalPath("shard::1;chainid::b5d7b697-f359-4ce5-a619-fa34b6fb01a5");
        assertEquals("apl-blockchain-shard-1-chain-b5d7b697-f359-4ce5-a619-fa34b6fb01a5.zip", pathToShardArchive.getFileName().toString());
        
//        String fpath = filesManager.mapFileIdToLocalPath("attachment::123;chainid::3ef0").toString();
//        assertEquals("123", fpath);
        
//        fpath = filesManager.mapFileIdToLocalPath("debug::123").toString();
//        assertEquals("123", fpath);
        
        // parse simple file name
//        fpath = filesManager.mapFileIdToLocalPath("file::phasing_poll.csv").toString();
//        assertEquals("123", fpath);;
        
//        assertEquals("phasing_poll.csv", pathToShardArchive.getFileName().toString());

        pathToShardArchive = filesManager.mapFileIdToLocalPath("shard::");
        assertNull(pathToShardArchive);

        assertThrows(NullPointerException.class, () -> filesManager.mapFileIdToLocalPath(null));

        pathToShardArchive = filesManager.mapFileIdToLocalPath("");
        assertNull(pathToShardArchive);
    }
}