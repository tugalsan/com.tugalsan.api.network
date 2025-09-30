package com.tugalsan.api.network.server.core;

import module com.tugalsan.api.charset;
import module com.tugalsan.api.list;
import module com.tugalsan.api.log;
import module com.tugalsan.api.stream;
import module com.tugalsan.api.union;
import module com.tugalsan.api.function;
import java.util.*;
import java.nio.file.*;

public class TS_NetworkCoreDirectoryUtils {

    final private static TS_Log d = TS_Log.of(TS_NetworkCoreDirectoryUtils.class);

    public static Path assureExists(Path path) {
        TS_NetworkCoreDirectoryUtils.createDirectoriesIfNotExists(path);
        if (!TS_NetworkCoreDirectoryUtils.isExistDirectory(path)) {
            TGS_FuncMTUUtils.thrw(d.className(), "assureExists", "!TS_DirectoryUtils.isExistDirectory(path)");
        }
        return path;
    }

    public static boolean isExistDirectory(Path directory) {
        return directory != null && Files.isDirectory(directory) && Files.exists(directory);
    }

    public static TGS_UnionExcuseVoid createDirectoriesIfNotExists(Path directory) {
        return TGS_FuncMTCUtils.call(() -> {
            if (!isExistDirectory(directory)) {
                directory.toFile().mkdirs();
                //return Files.createDirectories(directory);//BUGGY
            }
            return TGS_UnionExcuseVoid.ofVoid();
        }, e -> {
            return TGS_UnionExcuseVoid.ofExcuse(e);
        });
    }

    public static List<Path> subFiles(Path parentDirectory, CharSequence fileNameMatcher, boolean sorted, boolean recursive) {
        return TGS_StreamUtils.toLst(
                subFiles2(parentDirectory, fileNameMatcher, sorted, recursive)
                        .stream().map(str -> Path.of(str))
        );
    }

    //DONT TOUCH: ARRAYLIST<PATH> DOES NOT WORKING, DONT KNOW WHY!!
    public static List<String> subFiles2(Path parentDirectory, CharSequence fileNameMatcher, boolean sorted, boolean recursive) {
        return TGS_FuncMTCUtils.call(() -> {
            assureExists(parentDirectory);
            List<String> subFiles;
            if (fileNameMatcher == null) {
                if (recursive) {
                    subFiles = TGS_StreamUtils.toLst(
                            Files.walk(parentDirectory)
                                    .filter(p -> !Files.isDirectory(p)).map(p -> p.toString())
                    );
                } else {
                    subFiles = TGS_StreamUtils.toLst(
                            Files.list(parentDirectory)
                                    .filter(p -> !Files.isDirectory(p)).map(p -> p.toString())
                    );
                }
            } else {
                var fileNameMatcherStr = fileNameMatcher.toString();
                var matcher = FileSystems.getDefault().getPathMatcher("glob:**/" + fileNameMatcherStr);//"glob:*.java" or glob:**/*.java;
                var matcherUP = FileSystems.getDefault().getPathMatcher("glob:**/" + TGS_CharSetCast.current().toUpperCase(fileNameMatcherStr));//"glob:*.java" or glob:**/*.java;
                var matcherDW = FileSystems.getDefault().getPathMatcher("glob:**/" + TGS_CharSetCast.current().toUpperCase(fileNameMatcherStr));//"glob:*.java" or glob:**/*.java;
                if (recursive) {
                    subFiles = TGS_StreamUtils.toLst(
                            Files.walk(parentDirectory)
                                    .filter(p -> !Files.isDirectory(p) && (matcher.matches(p) || matcherUP.matches(p) || matcherDW.matches(p)))
                                    .map(p -> p.toString())
                    );
                } else {
                    subFiles = TGS_StreamUtils.toLst(
                            Files.list(parentDirectory)
                                    .filter(p -> !Files.isDirectory(p) && (matcher.matches(p) || matcherUP.matches(p) || matcherDW.matches(p)))
                                    .map(p -> p.toString())
                    );
                }
            }
            if (sorted) {
                subFiles = TGS_ListUtils.of(subFiles);
                Collections.sort(subFiles);
            }
            return subFiles;
        });
    }

}
