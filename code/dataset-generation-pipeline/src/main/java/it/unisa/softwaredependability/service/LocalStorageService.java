package it.unisa.softwaredependability.service;

import com.google.common.io.Files;
import it.unisa.softwaredependability.model.InMemoryFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class LocalStorageService extends FileService{

    private transient Logger log = Logger.getLogger(getClass().getName());

    @Override
    public File generateRepositoryCopy(List<InMemoryFile> files) throws IOException {
        File tempDir = Files.createTempDir();
        for(InMemoryFile imf: files) {
            File dir = new File(tempDir.getAbsolutePath() + "/" + imf.getPathWithoutFile());
            if(!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(tempDir.getAbsolutePath() + "/" + imf.getRelativePath());

            log.info(f.getAbsolutePath());

            if(f.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(imf.getContent());
                fos.flush();
                fos.close();
            }
        }
        return tempDir;
    }
}
