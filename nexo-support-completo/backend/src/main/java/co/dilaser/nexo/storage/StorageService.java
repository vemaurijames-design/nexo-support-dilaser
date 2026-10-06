package co.dilaser.nexo.storage;

import co.dilaser.nexo.config.NexoProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class StorageService {
    private final Path root;

    public StorageService(NexoProperties props) throws Exception {
        this.root = Path.of(props.getStorage().getPath()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file, String folder) throws Exception {
        Path dir = root.resolve(folder);
        Files.createDirectories(dir);
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = "";
        int i = original.lastIndexOf('.');
        if (i > 0) ext = original.substring(i);
        String name = UUID.randomUUID() + ext;
        Path dest = dir.resolve(name);
        Files.copy(file.getInputStream(), dest);
        return folder + "/" + name;
    }

    public Path resolve(String relative) {
        return root.resolve(relative).normalize();
    }
}
