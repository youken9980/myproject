package yk.core.util;

import com.jcraft.jsch.*;

import java.io.File;
import java.util.*;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class SftpUtil {

    private static final String CHANNEL_TYPE_SFTP = "sftp";
    private static final String PATH_SEPARATOR = "/";
    private static final Properties CONFIG = new Properties();

    static {
        CONFIG.put("StrictHostKeyChecking", "no");
    }

    private String host;
    private int port;
    private String username;
    private String password;
    private int timeout;
    private Session session;
    private ChannelSftp channel;

    public SftpUtil(String host, int port, String username, String password, int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    public void ensureConnect() {
        if (!isConnected()) {
            connect(CHANNEL_TYPE_SFTP);
        }
    }

    public void disconnect() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        System.out.println("Disconnect from " + getLoginInfo());
    }

    public boolean isRemoteFileExists(String remoteFile) {
        boolean isAlreadyConnected = isConnected();
        try {
            if (!isAlreadyConnected) {
                connect(CHANNEL_TYPE_SFTP);
            }
            Vector<?> vec = channel.ls(remoteFile);
            return !vec.isEmpty();
        } catch (SftpException e) {
            e.printStackTrace();
            throw new RuntimeException("Visit remote file " + remoteFile + " failure.", e);
        } finally {
            if (!isAlreadyConnected) {
                disconnect();
            }
        }
    }

    public void download(String remoteFile, String localFile) {
        boolean isAlreadyConnected = isConnected();
        try {
            if (!isAlreadyConnected) {
                connect(CHANNEL_TYPE_SFTP);
            }
            ensureLocalFilePath(localFile);
            channel.get(remoteFile, localFile);
            System.out.println("Download remote file " + remoteFile + " to local " + localFile + " success.");
        } catch (SftpException e) {
            e.printStackTrace();
            throw new RuntimeException("Download remote file " + remoteFile + " to local " + localFile + " failure.", e);
        } finally {
            if (!isAlreadyConnected) {
                disconnect();
            }
        }
    }

    public void upload(String localFile, String remoteFile) {
        boolean isAlreadyConnected = isConnected();
        try {
            if (!isAlreadyConnected) {
                connect(CHANNEL_TYPE_SFTP);
            }
            ensureRemoteFilePath(remoteFile);
            channel.put(localFile, remoteFile);
            System.out.println("Upload local file " + localFile + " to remote " + remoteFile + " success.");
        } catch (SftpException e) {
            e.printStackTrace();
            throw new RuntimeException("Upload local file " + localFile + " to remote " + remoteFile + " failure.", e);
        } finally {
            if (!isAlreadyConnected) {
                disconnect();
            }
        }
    }

    private void ensureLocalFilePath(String localFile) {
        File folder;
        if (localFile.endsWith(PATH_SEPARATOR)) {
            folder = new File(localFile);
        } else {
            folder = new File(localFile).getParentFile();
        }
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Local dir " + folder.getAbsolutePath() + " not exists, created success.");
            } else {
                throw new RuntimeException("Local dir " + folder.getAbsolutePath() + " created failure.");
            }
        }
    }

    private void ensureRemoteFilePath(String remoteFile) {
        int end = remoteFile.lastIndexOf(PATH_SEPARATOR);
        if (end < 0) {
            return;
        }
        // String realPath = remoteFile.substring(0, end);
        List<String> liPaths = splitFilePath(remoteFile);
        for (String path : liPaths) {
            try {
                channel.cd(path);
            } catch (SftpException e) {
                try {
                    channel.mkdir(path);
                    channel.cd(path);
                    System.out.println("Remote dir " + channel.pwd() + " not exists, created success.");
                } catch (SftpException e1) {
                    throw new RuntimeException("Remote dir " + path + " created failure.", e1);
                }
            }
        }
    }

    private List<String> splitFilePath(final String filePath) {
        List<String> list = new ArrayList<String>();
        int begin = 0;
        if (filePath.startsWith(PATH_SEPARATOR)) {
            begin = 1;
            list.add(PATH_SEPARATOR);
        }
        int end = filePath.lastIndexOf(PATH_SEPARATOR);
        if (end <= begin) {
            return list;
        }
        String realPath = filePath.substring(begin, end);
        StringTokenizer token = new StringTokenizer(realPath, PATH_SEPARATOR);
        while (token.hasMoreTokens()) {
            list.add(token.nextToken());
        }
        return list;
    }

    private boolean isConnected() {
        return session != null && session.isConnected() && channel != null && channel.isConnected();
    }

    private void connect(String type) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            if (password != null && password.length() > 0) {
                session.setPassword(password);
            }
            session.setConfig(CONFIG);
            session.setTimeout(timeout);
            session.connect();
            channel = (ChannelSftp) session.openChannel(type);
            channel.connect();
            System.out.println("Connect " + getLoginInfo() + " success.");
        } catch (JSchException e) {
            System.out.println("Connect " + getLoginInfo() + " failure.");
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String getLoginInfo() {
        StringBuffer stb = new StringBuffer();
        stb.append(username);
        stb.append("@");
        stb.append(host);
        stb.append(":");
        stb.append(port);
        return stb.toString();
    }
}
