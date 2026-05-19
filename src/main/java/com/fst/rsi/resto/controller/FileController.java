package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/file/")
public class FileController {

    @Value("${project.profile}")
    private String path;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }



    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileHandler(@RequestPart MultipartFile file) throws IOException{


        String uploadedFileName= fileService.uploadFile(path,file,file.getOriginalFilename());

        return ResponseEntity.ok("File Uploaded: "+uploadedFileName);

    }

    @GetMapping("/{fileName}")
    public void serveFileHandler(@PathVariable String fileName, HttpServletResponse response) throws IOException {

        InputStream resourceFile=fileService.getResourceFile(path,fileName);

        response.setContentType(MediaType.IMAGE_PNG_VALUE);

        StreamUtils.copy(resourceFile,response.getOutputStream());

    }
}

