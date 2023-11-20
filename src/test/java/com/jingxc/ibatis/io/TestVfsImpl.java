package com.jingxc.ibatis.io;

import com.jingxc.ibatis.logging.Log;
import com.jingxc.ibatis.logging.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class TestVfsImpl extends VFS {

    private static final Log log = LogFactory.getLog(TestVfsImpl.class);

    private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

    @Override
    public String toString() {
        return "sucess";
    }

    @Override
    protected List<String> list(URL url, String path) throws IOException {
        InputStream is = null;
        try {
            List<String> resources = new ArrayList<>();

            // First, try to find the URL of a JAR file containing the requested resource. If a JAR
            // file is found, then we'll list child resources by reading the JAR.
            URL jarUrl = findJarForResource(url);
            if (jarUrl != null) {
                is = jarUrl.openStream();
                if (log.isDebugEnabled()) {
                    log.debug("Listing " + url);
                }
                resources = listResources(new JarInputStream(is), path);
            } else {
                List<String> children = new ArrayList<>();
                try {
                    if (isJar(url)) {
                        // Some versions of JBoss VFS might give a JAR stream even if the resource
                        // referenced by the URL isn't actually a JAR
                        is = url.openStream();
                        try (JarInputStream jarInput = new JarInputStream(is)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Listing " + url);
                            }
                            for (JarEntry entry; (entry = jarInput.getNextJarEntry()) != null; ) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Jar entry: " + entry.getName());
                                }
                                children.add(entry.getName());
                            }
                        }
                    } else {
                        /*
                         * Some servlet containers allow reading from directory resources like a
                         * text file, listing the child resources one per line. However, there is no
                         * way to differentiate between directory and file resources just by reading
                         * them. To work around that, as each line is read, try to look it up via
                         * the class loader as a child of the current resource. If any line fails
                         * then we assume the current resource is not a directory.
                         */
                        is = url.openStream();
                        List<String> lines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                            for (String line; (line = reader.readLine()) != null; ) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Reader entry: " + line);
                                }
                                lines.add(line);
                                if (getResources(path + "/" + line).isEmpty()) {
                                    lines.clear();
                                    break;
                                }
                            }
                        } catch (InvalidPathException e) {
                            // #1974
                            lines.clear();
                        }
                        if (!lines.isEmpty()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Listing " + url);
                            }
                            children.addAll(lines);
                        }
                    }
                } catch (FileNotFoundException e) {
                    /*
                     * For file URLs the openStream() call might fail, depending on the servlet
                     * container, because directories can't be opened for reading. If that happens,
                     * then list the directory directly instead.
                     */
                    if ("file".equals(url.getProtocol())) {
                        File file = new File(url.getFile());
                        if (log.isDebugEnabled()) {
                            log.debug("Listing directory " + file.getAbsolutePath());
                        }
                        if (file.isDirectory()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Listing " + url);
                            }
                            children = Arrays.asList(file.list());
                        }
                    } else {
                        // No idea where the exception came from so rethrow it
                        throw e;
                    }
                }

                // The URL prefix to use when recursively listing child resources
                String prefix = url.toExternalForm();
                if (!prefix.endsWith("/")) {
                    prefix = prefix + "/";
                }

                // Iterate over immediate children, adding files and recursing into directories
                for (String child : children) {
                    String resourcePath = path + "/" + child;
                    resources.add(resourcePath);
                    URL childUrl = new URL(prefix + child);
                    resources.addAll(list(childUrl, resourcePath));
                }
            }

            return resources;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    protected URL findJarForResource(URL url) throws MalformedURLException {
        if (log.isDebugEnabled()) {
            log.debug("Find JAR URL: " + url);
        }

        // If the file part of the URL is itself a URL, then that URL probably points to the JAR
        boolean continueLoop = true;
        while (continueLoop) {
            try {
                url = new URL(url.getFile());
                if (log.isDebugEnabled()) {
                    log.debug("Inner URL: " + url);
                }
            } catch (MalformedURLException e) {
                // This will happen at some point and serves as a break in the loop
                continueLoop = false;
            }
        }

        // Look for the .jar extension and chop off everything after that
        StringBuilder jarUrl = new StringBuilder(url.toExternalForm());
        int index = jarUrl.lastIndexOf(".jar");
        if (index >= 0) {
            jarUrl.setLength(index + 4);
            if (log.isDebugEnabled()) {
                log.debug("Extracted JAR URL: " + jarUrl);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Not a JAR: " + jarUrl);
            }
            return null;
        }

        // Try to open and test it
        try {
            URL testUrl = new URL(jarUrl.toString());
            if (isJar(testUrl)) {
                return testUrl;
            } else {
                // WebLogic fix: check if the URL's file exists in the filesystem.
                if (log.isDebugEnabled()) {
                    log.debug("Not a JAR: " + jarUrl);
                }
                jarUrl.replace(0, jarUrl.length(), testUrl.getFile());
                File file = new File(jarUrl.toString());

                // File name might be URL-encoded
                if (!file.exists()) {
                    try {
                        file = new File(URLEncoder.encode(jarUrl.toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Unsupported encoding?  UTF-8?  That's unpossible.");
                    }
                }

                if (file.exists()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Trying real file: " + file.getAbsolutePath());
                    }
                    testUrl = file.toURI().toURL();
                    if (isJar(testUrl)) {
                        return testUrl;
                    }
                }
            }
        } catch (MalformedURLException e) {
            log.error("Invalid JAR URL: " + jarUrl);
        }

        if (log.isDebugEnabled()) {
            log.debug("Not a JAR: " + jarUrl);
        }
        return null;
    }

    protected List<String> listResources(JarInputStream jar, String path) throws IOException {
        // Include the leading and trailing slash when matching names
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        // Iterate over the entries and collect those that begin with the requested path
        List<String> resources = new ArrayList<>();
        for (JarEntry entry; (entry = jar.getNextJarEntry()) != null; ) {
            if (!entry.isDirectory()) {
                // Add leading slash if it's missing
                StringBuilder name = new StringBuilder(entry.getName());
                if (name.charAt(0) != '/') {
                    name.insert(0, '/');
                }

                // Check file name
                if (name.indexOf(path) == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found resource: " + name);
                    }
                    // Trim leading slash
                    resources.add(name.substring(1));
                }
            }
        }
        return resources;
    }

    protected boolean isJar(URL url) {
        return isJar(url, new byte[JAR_MAGIC.length]);
    }

    protected boolean isJar(URL url, byte[] buffer) {
        try (InputStream is = url.openStream()) {
            is.read(buffer, 0, JAR_MAGIC.length);
            if (Arrays.equals(buffer, JAR_MAGIC)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found JAR: " + url);
                }
                return true;
            }
        } catch (Exception e) {
            // Failure to read the stream means this is not a JAR
        }

        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
