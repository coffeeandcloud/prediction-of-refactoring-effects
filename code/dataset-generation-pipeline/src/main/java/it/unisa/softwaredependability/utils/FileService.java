package it.unisa.softwaredependability.utils;

import it.unisa.softwaredependability.model.InMemoryFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class FileService {
    public abstract File generateRepositoryCopy(List<InMemoryFile> files) throws IOException;
}
