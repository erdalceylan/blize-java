package com.blize.service.files;

import jakarta.servlet.ServletContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileService implements ServletContextAware {
    private ServletContext servletContext;

    @Override
    public void setServletContext(@NotNull ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public String getResourcesPath() {
        return System.getProperty("user.dir")+"/src/main/resources";
    }

    public File getFile(String fileName) {
        return new File(getResourcesPath() + fileName);
    }

    public String getFileContent(String fileName) throws IOException {
        return Files.readString(Paths.get(this.getResourcesPath() + fileName));
    }
}
