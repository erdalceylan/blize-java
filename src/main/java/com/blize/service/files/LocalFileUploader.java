package com.blize.service.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileUploader implements FileUploader {

    @Autowired
    private FileService fileService;

    @Override
    public FileInfo upload(Path file, String toRootPath, String toPath, String toFileName) {
        try{
            var targetPath = Paths.get(getStaticPath()+toRootPath+toPath+toFileName);
            createDir(targetPath.getParent());

            Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new FileInfo(getStaticPath(), toRootPath, toPath, toFileName, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDir(Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file);
        }
    }

    private String getStaticPath() {
        return this.fileService.getResourcesPath()+"/static";
    }
}
