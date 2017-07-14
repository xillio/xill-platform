package nl.xillio.xill.maven.services;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Map;

/**
 * Created by Dwight.Peters on 14-Jul-17.
 */
public class FileSystemFactory {
    public FileSystem createFileSystem(URI uri, Map<String, String> env) throws IOException {
        return FileSystems.newFileSystem(uri, env);
    }
}
