package com.saver.file.controllers;

import com.saver.file.exceptions.BadRequestException;
import com.saver.file.services.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static com.saver.file.controllers.Api.*;

@RestController
@RequestMapping(value = ROOT_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(UPLOAD)
    public UUID uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (!Objects.equals(file.getContentType(), MediaType.TEXT_PLAIN_VALUE)) {
            throw new BadRequestException("This type of file doesn't allowed!");
        }
        return fileService.uploadFile(file);
    }

    @GetMapping(DOWNLOAD)
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID key) throws IOException {
        fileService.downloadFile(key);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(fileService.downloadFile(key), headers, HttpStatus.OK);
    }
}
