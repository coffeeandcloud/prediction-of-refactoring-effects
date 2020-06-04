package it.unisa.softwaredependability.utils;

import it.unisa.softwaredependability.model.InMemoryFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class InMemoryStorageService extends FileService{

    //private final FileSystem fs;
    //private final Path root;

    public InMemoryStorageService() {
        //this.fs = Jimfs.newFileSystem(Configuration.unix());
        //this.root = fs.getPath("/");
    }

    @Override
    public File generateRepositoryCopy(List<InMemoryFile> files) throws IOException {
        /*
        String tempFileDir = "/" + UUID.randomUUID().toString();
        Path tempFileDirPath = fs.getPath(tempFileDir);
        Files.createDirectory(tempFileDirPath);
        for(InMemoryFile f: files) {
            Path filePath = tempFileDirPath.resolve(f.getFileName());
            Files.write(filePath, ImmutableList.of(new String(f.getContent())), StandardCharsets.UTF_8);
        }
        return tempFileDirPath.toFile();

         */
        return null;
    }
}
