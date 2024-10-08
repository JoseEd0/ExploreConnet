package dbp.exploreconnet.mediaStorage.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

    private final MediaStorageService mediaStorageService;

    @Autowired
    public FileUploadController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return mediaStorageService.uploadFile(file);
        } catch (Exception e) {
            return "Error al subir el archivo: " + e.getMessage();
        }
    }
}
