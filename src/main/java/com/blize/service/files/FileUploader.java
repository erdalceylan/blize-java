package com.blize.service.files;

import java.io.IOException;
import java.nio.file.Path;

public interface FileUploader {
    record FileInfo(String staticPath, String rootPath, String path, String fileName, Boolean remote) {
        public String getRoot(){
            return remote ? staticPath+rootPath : rootPath;
        }
        @Override
        public String toString(){
            return getRoot() +path +fileName;
        }
    }

    public FileInfo upload(Path file, String toRootPath, String toPath, String toFileName) throws IOException;
}
