package live.nickp0is0n.serverlogatsamp.network;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class FTPDownloader implements AutoCloseable{
    private String server;
    private String username;
    private String password;

    private boolean connected = false;

    private FTPClient ftpClient = new FTPClient();

    private Runnable onFtpConnectionListener = null;
    private Runnable onFtpLoginListener = null;
    private Runnable onFtpDownloadListener = null;

    public FTPDownloader(String server, String username, String password) {
        this.server = server;
        this.username = username;
        this.password = password;
    }

    @Override
    public void close() throws Exception {
        ftpClient.disconnect();
        connected = false;
    }

    public void connect() throws Exception {
        connectToServer();
        login();
        connected = true;
    }

    public void downloadFile(String remoteFileName, String localFileName) throws Exception {
        if (!connected) throw new Exception("Соединение с FTP сервером отсутствует!");
        if (onFtpDownloadListener != null) onFtpDownloadListener.run();
        File localFile = new File(localFileName);
        if (!localFile.exists()) localFile.createNewFile();
        ftpClient.retrieveFile(remoteFileName, new FileOutputStream(localFile));
        System.out.println(ftpClient.getReplyString());
    }

    public void setOnFtpConnectionListener(Runnable onFtpConnectedListener) {
        this.onFtpConnectionListener = onFtpConnectedListener;
    }

    public void setOnFtpLoginListener(Runnable onFtpLoginListener) {
        this.onFtpLoginListener = onFtpLoginListener;
    }

    public void setOnFtpDownloadListener(Runnable onFtpDownloadListener) {
        this.onFtpDownloadListener = onFtpDownloadListener;
    }

    private void connectToServer() throws Exception {
        if (onFtpConnectionListener != null) onFtpConnectionListener.run();
        ftpClient.connect(server);
        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) throw new Exception("Не удалось подключится к FTP серверу.");
        System.out.println(ftpClient.getReplyString());
    }

    private void login() throws Exception {
        if (onFtpLoginListener != null) onFtpConnectionListener.run();
        ftpClient.login(username, password);
        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) throw new Exception("Неправильный логин или пароль.");
        System.out.println(ftpClient.getReplyString());
        ftpClient.enterLocalPassiveMode();
        System.out.println(ftpClient.getReplyString());
    }

}
