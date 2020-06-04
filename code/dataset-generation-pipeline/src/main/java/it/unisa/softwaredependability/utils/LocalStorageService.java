package it.unisa.softwaredependability.utils;

import com.google.common.io.Files;
import it.unisa.softwaredependability.model.InMemoryFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LocalStorageService extends FileService{
    @Override
    public File generateRepositoryCopy(List<InMemoryFile> files) throws IOException {
        File tempDir = Files.createTempDir();
        for(InMemoryFile imf: files) {
            File f = new File(tempDir.getAbsolutePath() + "/" + imf.getFileName());
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
