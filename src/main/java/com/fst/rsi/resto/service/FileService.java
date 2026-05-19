package com.fst.rsi.resto.service;



import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {


    public String uploadFile(String path, MultipartFile file,String id) throws IOException {

        String fileName=id+file.getOriginalFilename();

        String filePath=path+ File.separator+fileName;

        File f=new File(path);
        if(!f.exists()){
            boolean isCreated=f.mkdir();
        }
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        return fileName ;
    }

    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {

        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        String filePath=path+ File.separator+fileName;
        return new FileInputStream(filePath);
    }
}
