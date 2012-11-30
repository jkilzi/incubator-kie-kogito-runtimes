/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droolsjbpm.services.impl.vfs;

import org.droolsjbpm.services.api.FileService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import org.droolsjbpm.services.api.FileException;
import org.kie.commons.java.nio.IOException;
import org.kie.commons.java.nio.file.DirectoryStream;
import org.kie.commons.java.nio.file.FileSystem;
import org.kie.commons.java.nio.file.FileSystemAlreadyExistsException;
import org.kie.commons.java.nio.file.FileSystems;
import org.kie.commons.java.nio.file.Files;
import org.kie.commons.java.nio.file.Path;
import org.kie.commons.java.nio.file.Paths;

/**
 *
 */
@ApplicationScoped
public class VFSFileServiceImpl implements FileService {

    private static final String REPO_PLAYGROUND = "git://jbpm-playground";
    private static final String ORIGIN_URL = "https://github.com/guvnorngtestuser1/jbpm-console-ng-playground.git";
    private FileSystem fileSystem = null;

    @Override
    public void checkFileSystem() {

        try {
            if (FileSystems.getFileSystem(URI.create(REPO_PLAYGROUND)) == null) {
                final String userName = "guvnorngtestuser1";
                final String password = "test1234";
                final URI fsURI = URI.create("git://jbpm-playground" + "?fetch");

                final Map<String, Object> env = new HashMap<String, Object>();
                env.put("username", userName);
                env.put("password", password);
                env.put("origin", ORIGIN_URL);
                fileSystem = FileSystems.newFileSystem(fsURI, env);
            }

        } catch (Exception e) {
            System.out.println(">>>>>>>>>>>>>>>>>>> E " + e.getMessage());
        }
    }

    public void fetchChanges() {
        fileSystem = FileSystems.getFileSystem(URI.create(REPO_PLAYGROUND + "?fetch"));
    }

    @Override
    public byte[] loadFile(final Path file) throws FileException {
        if (file == null) {
            throw new IllegalArgumentException();
        }

        checkFileSystem();

        if (!file.getFileSystem().equals(fileSystem)) {
            throw new IllegalStateException("file's fileSystem not supported.");
        }

        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new FileException(ex.getMessage(), ex);
        }
    }

    @Override
    public Iterable<Path> loadFilesByType(final String path, final String fileType) {
        checkFileSystem();

        return Files.newDirectoryStream(Paths.get("git://jbpm-playground/" + path), new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(final Path entry) throws IOException {
                if (entry.getFileName().toString().endsWith(fileType)) {
                    return true;
                }
                return false;
            }
        });
    }
}
