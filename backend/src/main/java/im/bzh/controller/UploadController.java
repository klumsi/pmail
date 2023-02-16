package im.bzh.controller;

import im.bzh.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${mail.file-upload-path}")
    private String uploadPath;

    @PostMapping
    public R upload(@RequestParam MultipartFile file) throws IOException {
        try {
            String fileName = file.getOriginalFilename();
            File directory = new File(uploadPath);
            File tempFile = File.createTempFile("temp_", "_" + fileName, directory);
            file.transferTo(tempFile);
            tempFile.deleteOnExit();
            return new R(true, null, tempFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return new R(false, "failed to upload", null);
        }
    }

}
