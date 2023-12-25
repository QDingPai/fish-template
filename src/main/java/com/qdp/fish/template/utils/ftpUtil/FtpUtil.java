package com.qdp.fish.template.utils.ftpUtil;

import com.qdp.fish.template.utils.ftpUtil.entity.FtpConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FtpUtil {
    /**
     * 文件编码
     */
    private static String CODE = "UTF-8";
    /**
     * ftp服务编码
     */
    private static String SERVER_CODE = "ISO-8859-1";

    /**
     * 获取FTP连接 <br>
     *
     * @param ftpConfig ftp服务器连接配置参数类 <br>
     * @return FTPClient对象 <br>
     */
    public static FTPClient getFTPConnect(FtpConfig ftpConfig) {
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            //1.连接服务器
            ftp.connect(ftpConfig.getHost(), ftpConfig.getPort());
            //2.登录服务器 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftp.enterLocalPassiveMode();
            //3.判断登陆是否成功
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return null;
            }
            return ftp;
        } catch (IOException e) {
            log.error("getFTPConnect error msg:{}", e.getMessage(), e);
            closeFTP(ftp);
        }
        return null;
    }

    /**
     * 关闭FTP连接,释放资源 <br>
     *
     * @param ftpClient 连接对象 <br>
     */
    public static void closeFTP(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                log.error("closeFTP error msg:{}", e.getMessage(), e);
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.error("closeFTP error msg:{}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Description: 向FTP服务器上传文件 <br>
     *
     * @param ftpConfig  FTP服务器连接配置参数类 <br>
     * @param remotePath FTP服务器文件目录 <br>
     * @param fileName   上传到FTP服务器上的文件名 <br>
     * @param input      输入流 <br>
     * @return 成功返回true，否则返回false <br>
     */
    public static boolean uploadFile(FtpConfig ftpConfig, String remotePath, String fileName, InputStream input) {
        FTPClient ftp = null;
        try {
            ftp = getFTPConnect(ftpConfig);
            if (ftp == null) {
                return false;
            }
            if (StringUtils.isNotBlank(remotePath)) {
                remotePath = remotePath.replace("\\", "/");
            }
            //切换到上传目录
            if (!ftp.changeWorkingDirectory(remotePath)) {
                //如果目录不存在创建目录
                String[] dirs = remotePath.split("/");
                String path = "";
                for (String dir : dirs) {
                    if (StringUtils.isBlank(dir)) continue;
                    if (remotePath.startsWith("/")) {
                        path += "/" + dir;
                    } else {
                        if (StringUtils.isBlank(path)) {
                            path += dir;
                        } else {
                            path += "/" + dir;
                        }
                    }
                    //进不去目录，说明该目录不存在
                    if (!ftp.changeWorkingDirectory(path)) {
                        //创建目录
                        if (!ftp.makeDirectory(path)) {
                            //如果创建文件目录失败，则返回
                            log.info("创建文件目录" + path + "失败");
                            return false;
                        } else {
                            //目录存在，则直接进入该目录
                            ftp.changeWorkingDirectory(path);
                        }
                    }
                }
            }
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //上传文件
            fileName = new String(fileName.getBytes(CODE), SERVER_CODE);
            if (!ftp.storeFile(fileName, input)) {
                return false;
            }
            return true;
        } catch (IOException e) {
            log.error("uploadFile error msg:{}", e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(input);
            closeFTP(ftp);
        }
        return false;
    }

    /**
     * Description: 向FTP服务器上传本地文件 <br>
     *
     * @param ftpConfig  FTP服务器连接配置参数类 <br>
     * @param remotePath FTP服务器文件目录 <br>
     * @param localPath  本地文件目录 <br>
     * @param fileName   本地文件名及上传到FTP服务器上的文件名 <br>
     * @return 成功返回true，否则返回false <br>
     */
    public static boolean uploadFile(FtpConfig ftpConfig, String remotePath, String localPath, String fileName) {
        InputStream inputStream = null;
        try {
            String filePath = localPath + fileName;
            // 创建一个文件对象
            File file = new File(filePath);
            // 创建文件输入流
            inputStream = new FileInputStream(file);
            return uploadFile(ftpConfig, remotePath, fileName, inputStream);
        } catch (Exception e) {
            log.error("uploadFile error msg:{}", e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return false;
    }

    /**
     * Description: 从FTP服务器下载文件到输入流 <br>
     *
     * @param ftpConfig  FTP服务器连接配置参数类 <br>
     * @param remotePath FTP服务器上的相对路径 <br>
     * @param fileName   要下载的文件名 <br>
     * @return 结果: 文件输入流 <br>
     */
    public static InputStream downloadFile(FtpConfig ftpConfig, String remotePath, String fileName) {
        FTPClient ftp = null;
        try {
            ftp = getFTPConnect(ftpConfig);
            if (ftp == null) {
                return null;
            }
            ftp.setControlEncoding(CODE);
            ftp.setFileType(ftp.BINARY_FILE_TYPE);
            ftp.setBufferSize(1024);
            // 转移到FTP服务器目录
            ftp.changeWorkingDirectory(remotePath);
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    String remoteAbsoluteFile = ff.getName();
                    remoteAbsoluteFile = new String(remoteAbsoluteFile.getBytes(CODE), SERVER_CODE);
                    InputStream inputStream = ftp.retrieveFileStream(remoteAbsoluteFile);
                    return inputStream;
                }
            }
        } catch (IOException e) {
            log.error("downloadFile error msg:{}", e.getMessage(), e);
        } finally {
            closeFTP(ftp);
        }
        return null;
    }

    /**
     * Description: 从FTP服务器下载文件到本地 <br>
     *
     * @param ftpConfig  FTP服务器连接配置参数类 <br>
     * @param remotePath FTP服务器上的相对路径 <br>
     * @param localPath  本地服务器上的相对路径 <br>
     * @param fileName   要下载的文件名 <br>
     * @return
     */
    public static boolean downloadFile(FtpConfig ftpConfig, String remotePath, String localPath, String fileName) {
        String filePath = localPath + fileName;
        try (
                InputStream inputStream = downloadFile(ftpConfig, remotePath, fileName);
                OutputStream outputStream = new FileOutputStream(filePath);
        ) {
            if (inputStream == null) {
                return false;
            }
            IOUtils.copy(inputStream, outputStream);
            return true;
        } catch (Exception e) {
            log.error("downloadFile error msg:{}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Description: 从FTP服务器下载文件到本地 <br>
     *
     * @param ftpConfig  FTP服务器连接配置参数类 <br>
     * @param remotePath FTP服务器上的相对路径 <br>
     * @param fileName   要下载的文件名 <br>
     * @param request    请求对象 <br>
     * @param response   响应对象 <br>
     * @return
     */
    public static void downloadFile(FtpConfig ftpConfig, String remotePath, String fileName, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        try (
                InputStream inputStream = downloadFile(ftpConfig, remotePath, fileName);
                ServletOutputStream outputStream = response.getOutputStream();
        ) {
            if (inputStream == null) {
                return;
            }
            IOUtils.copy(inputStream, outputStream);
            String agent = request.getHeader("User-Agent").toUpperCase();
            // 指定编码格式
            String newFileName = "";
            if (agent.indexOf("FIREFOX") > 0) {
                newFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            } else {
                newFileName = java.net.URLEncoder.encode(fileName, "utf-8");
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + newFileName);
        } catch (Exception e) {
            log.error("downloadFile error msg:{}", e.getMessage(), e);
        }
    }

    /**
     * 删除ftp中文件
     *@param ftpConfig  FTP服务器连接配置参数类 <br>
     * @param path     文件所在路径
     * @param pathname 文件全路径或文件名
     * @return
     */
    public static boolean deleteFile(FtpConfig ftpConfig, String path, String pathname) {
        FTPClient ftp = null;
        try {
            ftp = getFTPConnect(ftpConfig);
            if (ftp != null) {
                if (path != null && path.trim().length() > 0) {
                    if (pathname != null && pathname.trim().length() > 0) {
                        ftp.setControlEncoding(CODE);
                        ftp.changeWorkingDirectory(path);
                        String s = ftp.printWorkingDirectory();
                        FTPFile[] ftpFiles = ftp.listFiles();
                        for (FTPFile ftpFile : ftpFiles) {
                            if (ftpFile.getName().equals(pathname)) {
                                return ftp.deleteFile(new String(pathname.getBytes(CODE), SERVER_CODE));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("deleteFile error msg:{}", e.getMessage(), e);
        } finally {
            closeFTP(ftp);
        }
        return false;
    }

}

