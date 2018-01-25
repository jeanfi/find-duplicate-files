package net.wpitchoune.duplicate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

public final class Main {
        private static final String CLASS_NAME = Main.class.getName();
        private static final Logger logger = Logger.getLogger(CLASS_NAME);
        
        private static byte[] computeHash(Path p, String algo) throws NoSuchAlgorithmException, IOException {
                MessageDigest md;
                byte[] buf, ret;
                int n;
                final String METHOD_NAME = "computeHash";
                
                logger.entering(CLASS_NAME, METHOD_NAME, p);
                
                md = MessageDigest.getInstance(algo);
                
                try (InputStream is = Files.newInputStream(p)) {                     
                        buf = new byte[1024];
                        while ((n = is.read(buf)) > 0)
                                md.update(buf, 0, n);
                }                
                                
                ret = md.digest();
                
                logger.exiting(CLASS_NAME, METHOD_NAME, ret);

                return ret;
        }
        
        public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
                Map<String, Path> files;
                
                files = new HashMap<>();
                
                for (String p: args) {
                        Files.walkFileTree(Paths.get(p), new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path fp, BasicFileAttributes attrs) throws IOException {
                                        byte[] h;
                                        String strh;
                                        Path dupe;

                                        if (!attrs.isSymbolicLink() && attrs.isRegularFile() && attrs.size() > 0) {                                                
                                                try {
                                                        h = computeHash(fp, "SHA-256");
                                                        strh = DatatypeConverter.printHexBinary(h);
                                                        
                                                        dupe = files.get(strh);
                                                        if (dupe != null)
                                                                System.out.println(fp.toRealPath() + " is a duplicate of " + dupe.toRealPath());
                                                        else
                                                                files.put(strh, fp);
                                                } catch (NoSuchAlgorithmException | IOException e) {
                                                        logger.log(Level.SEVERE, "Failed to compute hash of " + fp, e);
                                                }
                                        }
                                        return FileVisitResult.CONTINUE;
                                }
                                
                                @Override
                                public FileVisitResult visitFileFailed(Path fp, IOException exc) throws IOException {
                                        System.out.println("Cannot explore: "+ fp);
                                        return FileVisitResult.CONTINUE;
                                }
                        });
                }
        }
}
