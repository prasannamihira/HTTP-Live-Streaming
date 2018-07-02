package com.acromax.acromaxmediaplayer.model;

import java.io.File;

public class AudioChunk {

    private int chunkOffSet;
    private int chunkLength;
    private String fileName;
    private String chunkFileName;
    private String fullPath;
    private File fileStream;

    public int getChunkOffSet() {
        return chunkOffSet;
    }

    public void setChunkOffSet(int chunkOffSet) {
        this.chunkOffSet = chunkOffSet;
    }

    public int getChunkLength() {
        return chunkLength;
    }

    public void setChunkLength(int chunkLength) {
        this.chunkLength = chunkLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getChunkFileName() {
        return chunkFileName;
    }

    public void setChunkFileName(String chunkFileName) {
        this.chunkFileName = chunkFileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public File getFileStream() {
        return fileStream;
    }

    public void setFileStream(File fileStream) {
        this.fileStream = fileStream;
    }
}
