package com.saver.file.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saver.file.dto.RedisFileBytesDto;
import com.saver.file.dto.RedisResponseDto;
import com.saver.file.props.AppProps;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static java.nio.file.Files.exists;

@Service
@CommonsLog
public class FileService {

    private final AppProps appProps;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private static Path LOG_FILE;

    public FileService(AppProps appProps, RedisService redisService, ObjectMapper objectMapper) {
        this.appProps = appProps;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        LOG_FILE = Paths.get(appProps.getLogStorage() + "/log.txt");
    }

    @PostConstruct
    private void init() throws IOException {
        if (!exists(appProps.getFileStorage())) {
            log.info("Make a file storage");
            Files.createDirectories(appProps.getFileStorage());
        }

        if (!exists(LOG_FILE.getParent())) {
            log.info("Make a log storage");
            Files.createDirectories(LOG_FILE.getParent());
            Files.createFile(LOG_FILE);
        }
    }

    public byte[] downloadFile(UUID key) throws IOException {
        RedisResponseDto responseDto = Files.lines(LOG_FILE).map(this::parseLine)
                .filter(item -> Objects.equals(item.getKey(), key))
                .peek(log::info)
                .findFirst()
                .orElseThrow(FileSystemNotFoundException::new);
        return redisService.downloadFile(responseDto.getApp_id(), key);
    }

    private RedisResponseDto parseLine(String item) {
        try {
            return objectMapper.readValue(item, RedisResponseDto.class);
        } catch (IOException e) {
            log.error("Can't parse line!", e);
            throw new RuntimeException(e);
        }
    }

    public UUID uploadFile(MultipartFile file) throws IOException {
        RedisFileBytesDto requestDto = RedisFileBytesDto.builder()
                .bytes(file.getBytes())
                .build();
        RedisResponseDto responseDto = redisService.publishFile(requestDto);
        writeLog(responseDto);
        return responseDto.getKey();
    }

    private void writeLog(RedisResponseDto responseDto) throws IOException {
        String line = objectMapper.writeValueAsString(responseDto);
        log.info("Write line: " + line);
        Files.write(LOG_FILE, Collections.singletonList(line), StandardOpenOption.APPEND);
    }
}
