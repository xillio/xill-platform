package nl.xillio.xill.maven.services;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * Created by Dwight.Peters on 14-Jul-17.
 */
public class FilesService {
    public Path walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException {
        return Files.walkFileTree(start, visitor);
    }

    public Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException{
        return Files.createDirectories(dir, attrs);
    }

    public Path copy(Path source, Path target, CopyOption... options) throws IOException{
        return Files.copy(source, target, options);
    }
}
