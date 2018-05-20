package com.saver.file.services;

import com.saver.file.dto.RedisFileBytesDto;
import com.saver.file.dto.RedisResponseDto;
import com.saver.file.props.AppProps;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;
import ua.ardas.redis.client.RedisClientTemplate;
import ua.ardas.redis.client.dto.RedisResponse;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;

@Service
@CommonsLog
public class RedisService {
    public static final String UPLOAD_FILE_CHANNEL = "UPLOAD_FILE_CHANNEL";
    public static String DOWNLOAD_FILE_CHANNEL = "DOWNLOAD_FILE_CHANNEL";

    private final RedisClientTemplate redisClientTemplate;
    private final AppProps appProps;

    public RedisService(RedisClientTemplate redisClientTemplate, AppProps appProps) {
        this.redisClientTemplate = redisClientTemplate;
        this.appProps = appProps;
    }

    @PostConstruct
    public void startListener() {
        startUploadListener();
        redisClientTemplate.listenChannel(DOWNLOAD_FILE_CHANNEL + "-" + appProps.getId(), this::downloadListener, UUID.class);
    }

    private void startUploadListener() {
        redisClientTemplate.listenChannel(UPLOAD_FILE_CHANNEL, this::uploadListener, RedisFileBytesDto.class);
    }

    private RedisResponseDto uploadListener(RedisFileBytesDto dto) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(dto.getBytes());
        UUID fileName = UUID.randomUUID();
        Path fileTempPath = appProps.getFileStorage().resolve(fileName.toString());
        try {
            Files.copy(inputStream, fileTempPath);
        } catch (IOException e) {
            log.error("Can't save file!", e);
            throw new RuntimeException(e.getMessage());
        }
        return RedisResponseDto.builder()
                .app_id(appProps.getId())
                .key(fileName)
                .build();
    }

    public RedisResponseDto publishFile(RedisFileBytesDto messageDto) throws IOException {
        redisClientTemplate.stopListenChannel(UPLOAD_FILE_CHANNEL);
        RedisResponse<RedisResponseDto> response = redisClientTemplate.send(UPLOAD_FILE_CHANNEL, messageDto, RedisResponseDto.class);
        if (response.isFailure()) {
            startUploadListener();
            throw new RuntimeException(response.getMessage());
        }
        startUploadListener();
        return response.getBody();
    }

    private byte[] downloadListener(UUID fileId) {
        Path filePath = appProps.getFileStorage().resolve(fileId.toString());
        if (!exists(filePath)) {
            throw new FileSystemNotFoundException("File doesn't exist!");
        }
        try {
            return readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Can't read file!", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] downloadFile(UUID appId, UUID key) throws IOException {
        RedisResponse<byte[]> response = redisClientTemplate.send(DOWNLOAD_FILE_CHANNEL + "-" + appId, key, byte[].class);
        if (response.isFailure()) {
            throw new RuntimeException(response.getMessage());
        }
        return response.getBody();
    }
}
