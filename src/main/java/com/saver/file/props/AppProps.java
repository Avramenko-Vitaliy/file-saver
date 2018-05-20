package com.saver.file.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Data
@ConfigurationProperties("app")
public class AppProps {
    private UUID id;
    private String fileStorage;
    private String logStorage;

    public Path getFileStorage() {
        return Paths.get(fileStorage + "/" + id);
    }

    public Path getLogStorage() {
        return Paths.get(logStorage + "/" + id);
    }
}
