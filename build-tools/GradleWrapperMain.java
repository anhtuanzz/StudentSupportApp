// Minimal self-contained launcher for educational archive; not the official Gradle wrapper.
// Downloads the distribution specified in gradle-wrapper.properties and executes it.
package org.gradle.wrapper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public final class GradleWrapperMain {
    public static void main(String[] args) throws Exception {
        Path jar = Paths.get(GradleWrapperMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path project = jar.getParent().getParent().getParent();
        Path propertiesPath = project.resolve("gradle/wrapper/gradle-wrapper.properties");
        Properties properties = new Properties();
        try(InputStream in = Files.newInputStream(propertiesPath)) { properties.load(in); }
        String distribution = properties.getProperty("distributionUrl");
        if(distribution == null || !distribution.startsWith("https://services.gradle.org/distributions/gradle-")) {
            throw new IllegalArgumentException("Expected official HTTPS services.gradle.org distribution URL");
        }
        String name = distribution.substring(distribution.lastIndexOf('/') + 1).replace("-bin.zip", "");
        Path cache = Paths.get(System.getProperty("user.home"), ".gradle", "student-support-bootstrap", name);
        String scriptName = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "gradle.bat" : "gradle";
        Path executable = cache.resolve(name).resolve("bin").resolve(scriptName);
        if(!Files.exists(executable)) {
            Files.createDirectories(cache);
            Path tempZip = cache.resolve("gradle-download.zip.part");
            System.out.println("Downloading Gradle: " + distribution);
            URL url = new URL(distribution);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            try(InputStream in = conn.getInputStream(); OutputStream out = Files.newOutputStream(tempZip)) {
                byte[] buffer = new byte[65536];int n;
                while((n=in.read(buffer))>=0)out.write(buffer,0,n);
            }
            try(ZipInputStream in = new ZipInputStream(Files.newInputStream(tempZip))) {
                ZipEntry entry;
                while((entry=in.getNextEntry())!=null){
                    Path target = cache.resolve(entry.getName()).normalize();
                    if(!target.startsWith(cache))throw new IOException("Invalid zip entry: "+entry.getName());
                    if(entry.isDirectory())Files.createDirectories(target);
                    else {
                        Files.createDirectories(target.getParent());
                        Files.copy(in,target,StandardCopyOption.REPLACE_EXISTING);
                    }
                    in.closeEntry();
                }
            }
            Files.deleteIfExists(tempZip);
        }
        if(!Files.exists(executable))throw new IOException("Gradle executable not found: "+executable);
        executable.toFile().setExecutable(true);
        List<String> cmd = new ArrayList<>();
        boolean win = scriptName.endsWith(".bat");
        if(win){cmd.add("cmd");cmd.add("/c");}
        cmd.add(executable.toAbsolutePath().toString());
        Collections.addAll(cmd,args);
        Process p = new ProcessBuilder(cmd).directory(project.toFile()).inheritIO().start();
        System.exit(p.waitFor());
    }
}
