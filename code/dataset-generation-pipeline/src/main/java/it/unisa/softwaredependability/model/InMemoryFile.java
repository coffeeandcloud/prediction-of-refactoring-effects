package it.unisa.softwaredependability.model;


public class InMemoryFile {
    private byte[] content;
    private String path;

    public InMemoryFile(byte[] content, String path) {
        this.content = content;
        this.path = path;
    }

    public byte[] getContent() {
        return content;
    }

    public String getRelativePath() {
        return path;
    }

    public String getFileName() {
        String[] pathParts = path.split("/");
        if(pathParts.length == 0) return null;
        return pathParts[pathParts.length-1];
    }
}
