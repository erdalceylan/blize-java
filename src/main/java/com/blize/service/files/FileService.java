package com.blize.service.files;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class FileService {
    private ApplicationContext applicationContext;

    public FileService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public String getResourcesPath() {
        return System.getProperty("user.dir")+"/src/main/resources";
    }

    public File getFile(String fileName) throws IOException {
        return getApplicationContext().getResource("classpath:" + fileName).getFile();
    }

    public String getFileContent(String fileName) throws IOException {
        var inputStream = getApplicationContext().getResource("classpath:" + fileName).getInputStream();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
