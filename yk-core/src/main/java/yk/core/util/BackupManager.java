package yk.core.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class BackupManager {

    private static final long B = 1L;
    private static final long KB = 1024 * B;
    private static final long MB = 1024 * KB;
    private static final long GB = 1024 * MB;
    private static final long TB = 1024 * GB;

    private static final List<String> EXCLUDES;
    private static final FileFilter FILE_FILTER;

    static {
        EXCLUDES = new ArrayList<String>();
        EXCLUDES.add(".DS_Store");
        FILE_FILTER = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !EXCLUDES.contains(pathname.getName());
            }
        };
    }

    public void backup(String[] args) {
        if (args.length < 2) {
            System.exit(0);
        }
        long lBegin = System.currentTimeMillis();
        try {
            int argLength = args.length;
            if (argLength > 2) {
                for (int i = 2; i < argLength; i++) {
                    sync(concat(args[0], args[i]), concat(args[1], args[i]));
                }
            } else {
                sync(args[0], args[1]);
            }
        } finally {
            long lEnd = System.currentTimeMillis();
            double dCostSs = (lEnd - lBegin) / 1000.0;
            double dCostMm = dCostSs / 60.0;
            System.out.println(String.format("Cost: %1$.2fm, %2$.2fs.", dCostMm, dCostSs));
        }
    }

    private String concat(String path, String filename) {
        String str = path;
        if (!str.endsWith(File.separator)) {
            str = str.concat(File.separator);
        }
        return str.concat(filename);
    }

    private void sync(String src, String dest) {
        try {
            if (src == null || dest == null) {
                return;
            }
            File fileSrc = new File(src);
            if (!fileSrc.exists()) {
                return;
            }
            File fileDest = new File(dest);
            if (fileSrc.isFile()) {
                // 1. src is file.
                if (fileDest.isDirectory()) {
                    // 1.1. if dest is folder, remove folder, then copy file.
                    print("rm", fileDest);
                    FileUtils.deleteDirectory(fileDest);
                } else if (fileDest.isFile()) {
                    // 1.2. if dest is file, ignore equals, overwrite otherwise.
                    if (equals(fileSrc, fileDest)) {
                        return;
                    }
                }
                // 1.3. ignore else, dest does not exists, no matter.
                print("cp", fileSrc);
                FileUtils.copyFile(fileSrc, fileDest);
            } else if (fileSrc.isDirectory()) {
                // 2. src is folder.
                if (fileDest.isDirectory()) {
                    // 2.1. dest is folder.
                    Map<String, File> mapSrc = list(fileSrc, FILE_FILTER);
                    Map<String, File> mapDest = list(fileDest, null);
                    // remove not exist files.
                    List<File> toRemove = notExistFiles(mapSrc, mapDest);
                    for (File f : toRemove) {
                        print("rm", f);
                        FileUtils.deleteQuietly(f);
                    }
                    // recursive
                    for (File f : mapSrc.values()) {
                        sync(f.getAbsolutePath(), getDestPath(dest, f));
                    }
                } else if (fileDest.isFile()) {
                    // 2.2. if dest is file, remove file, then copy folder.
                    print("rm", fileDest);
                    FileUtils.deleteQuietly(fileDest);
                    print("cp", fileSrc);
                    FileUtils.copyDirectory(fileSrc, fileDest, FILE_FILTER);
                } else {
                    // 2.3. dest does not exists, copy folder.
                    print("cp", fileSrc);
                    FileUtils.copyDirectory(fileSrc, fileDest, FILE_FILTER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDestPath(String dest, File srcFile) {
        String path = dest;
        if (!dest.endsWith(File.separator)) {
            path = path.concat(File.separator);
        }
        return path.concat(srcFile.getName());
    }

    private List<File> notExistFiles(Map<String, File> src, Map<String, File> dest) {
        if (src == null || dest == null) {
            return null;
        }

        // separate files which not exists in src to remove.
        List<File> list = new ArrayList<File>();
        File file;
        for (String fileName : dest.keySet()) {
            file = src.get(fileName);
            if (file == null) {
                list.add(dest.get(fileName));
            }
        }
        return list;
    }

    private Map<String, File> list(File file, FileFilter filter) {
        if (file == null || !file.exists()) {
            return null;
        }
        File[] files = file.listFiles(filter);
        if (files == null) {
            return null;
        }
        Map<String, File> map = new LinkedHashMap<String, File>();
        for (File f : files) {
            map.put(f.getName(), f);
        }
        return map;
    }

    private boolean equals(File src, File dest) {
        return src != null && dest != null
                && src.getName().equals(dest.getName())
                && (src.length() == dest.length())
                && (src.lastModified() == dest.lastModified());
    }

    private void print(String op, File file) {
        System.out.println(String.format(
                "%s\t%s\t%s", op, formatSize(getSize(file, 0L)), file.getAbsolutePath()
        ));
    }

    private long getSize(File file, long size) {
        if (file == null) {
            return 0L;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size = getSize(f, size);
                }
            }
        } else {
            size += file.length();
        }
        return size;
    }

    private String formatSize(long size) {
        String str;
        if (size < KB) {
            str = String.format("%1$7.2f B", (double) size);
        } else if (size < MB) {
            str = String.format("%1$7.2fKB", (double) size / KB);
        } else if (size < GB) {
            str = String.format("%1$7.2fMB", (double) size / MB);
        } else if (size < TB) {
            str = String.format("%1$7.2fGB", (double) size / GB);
        } else {
            str = String.format("%1$7.2fTB", (double) size / TB);
        }
        return str;
    }
}
