package net.wpitchoune.duplicate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

public class DuplicateFinder {
        private static final String FASTER_HASH_ALGO = "MD5";
        private static final String SAFER_HASH_ALGO = "SHA-256";
        private static final String CLASS_NAME = DuplicateFinder.class.getName();
        private static final Logger logger = Logger.getLogger(CLASS_NAME);
        private final Map<String, Path> fastHashes = new HashMap<>();
        private final Map<Path, String> safeHashes = new HashMap<>();
        private final Map<Path, List<Path>> allDupes = new HashMap<>();
        
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
        
        public Map<Path, List<Path>> getDuplicates() {
                return allDupes;
        }

        private String getSaferHash(Path p) throws NoSuchAlgorithmException, IOException {
                String ret;
                
                ret = safeHashes.get(p);
                
                if (ret != null)
                        return ret;
                
                ret = DatatypeConverter.printHexBinary(computeHash(p, SAFER_HASH_ALGO));
                safeHashes.put(p, ret);
                
                return ret;
        }
        
        private boolean equals(Path p1, Path p2) throws NoSuchAlgorithmException, IOException {
                return getSaferHash(p1).equals(getSaferHash(p2));
        }
        
        public void add(Path fp) {
                byte[] h;
                String fastHash;
                Path dupe;
                List<Path> dupes;
                
                try {
                        h = computeHash(fp, FASTER_HASH_ALGO);
                        fastHash = DatatypeConverter.printHexBinary(h);
                        
                        dupe = fastHashes.get(fastHash);
                        if (dupe != null) {
                                if (equals(fp, dupe)) {
                                        dupes = allDupes.get(dupe);
                                        if (dupes == null) {
                                                dupes = new ArrayList<>();
                                                allDupes.put(dupe, dupes);
                                        }
                                        dupes.add(fp);
                                }
                        } else {
                                fastHashes.put(fastHash, fp);
                        }
                } catch (NoSuchAlgorithmException | IOException e) {
                        logger.log(Level.SEVERE, "Failed to compute hash of " + fp, e);
                }
        }
}
