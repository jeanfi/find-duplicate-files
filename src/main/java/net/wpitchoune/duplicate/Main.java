package net.wpitchoune.duplicate;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class Main {
        private static final String CLASS_NAME = Main.class.getName();
        private static final Logger logger = Logger.getLogger(CLASS_NAME);
        
        public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
                DuplicateFinder finder;
                long duplicatesCount;
                
                finder = new DuplicateFinder();
                
                for (String p: args) {
                        Files.walkFileTree(Paths.get(p), new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path fp, BasicFileAttributes attrs) throws IOException {

                                        if (!attrs.isSymbolicLink() && attrs.isRegularFile() && attrs.size() > 0)       
                                                finder.add(fp);
                                        
                                        return FileVisitResult.CONTINUE;
                                }
                                
                                @Override
                                public FileVisitResult visitFileFailed(Path fp, IOException exc) throws IOException {
                                        System.out.println("Cannot explore: "+ fp);
                                        return FileVisitResult.CONTINUE;
                                }
                        });
                }
                
                duplicatesCount = 0;
                for (Map.Entry<Path, List<Path>> e: finder.getDuplicates().entrySet()) {
                        System.out.println(e.getValue().size() + "\t" + e.getKey());
                        for (Path p: e.getValue())
                                System.out.println("\t" + p);
                        duplicatesCount += e.getValue().size();
                }
                
                System.out.println("Number of files with duplicates: " + finder.getDuplicates().size());
                System.out.println("Number of duplicates: " + duplicatesCount);
        }
}
