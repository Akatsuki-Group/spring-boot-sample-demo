package com.github.lybgeek.upload.util;


import com.github.lybgeek.common.exception.BizException;
import com.github.lybgeek.upload.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FileUtil extends FileUtils {

  public static final String PATH_HEAD = "/vagrant/";
  public static final String USER_HEAD = "/data/User";
  public static final String DOT = ".";
  public static final String SLASH_ONE = "/";
  public static final String SLASH_TWO = "\\";
  public static final String HOME = "";
  private static String uploadWindowRoot;



  public static void moveFiles(String oldPath, String newPath) throws IOException {

    String[] filePaths = new File(oldPath).list();

    if (filePaths != null && filePaths.length > 0) {
      if (!new File(newPath).exists()) {
        new File(newPath).mkdirs();
      }

      for (int i = 0; i < filePaths.length; i++) {
        if (new File(oldPath + File.separator + filePaths[i]).isDirectory()) {
          moveFiles(oldPath + File.separator + filePaths[i],
              newPath + File.separator + filePaths[i]);
        } else if (new File(oldPath + File.separator + filePaths[i]).isFile()) {
          copyFile(oldPath + File.separator + filePaths[i],
              newPath + File.separator + filePaths[i]);
          new File(oldPath + File.separator + filePaths[i])
              .renameTo(new File(newPath + File.separator + filePaths[i]));
        }
      }
    }
  }

  public static void copyFile(String oldPath, String newPath) {

    try {
      File oldFile = new File(oldPath);
      File file = new File(newPath);
      FileInputStream in = new FileInputStream(oldFile);
      FileOutputStream out = new FileOutputStream(file);
      byte[] buffer = new byte[2097152];

      while ((in.read(buffer)) != -1) {
        out.write(buffer);
      }
    } catch (IOException e) {
      throw new BizException("??????????????????", e);
    }
  }

  /**
   * ??????????????????????????????????????????path?????????
   */
  public static String getFileName(String fileName) {

    String name = "";
    if (StringUtils.lastIndexOf(fileName, SLASH_ONE) >= StringUtils
        .lastIndexOf(fileName, SLASH_TWO)) {
      name = StringUtils
          .substring(fileName, StringUtils.lastIndexOf(fileName, SLASH_ONE) + 1,
              fileName.length());

    } else {
      name = StringUtils
          .substring(fileName, StringUtils.lastIndexOf(fileName, SLASH_TWO) + 1,
              fileName.length());
    }
    return StringUtils.trimToEmpty(name);
  }

  /**
   * ?????????????????????????????????
   */
  public static String getWithoutExtension(String fileName) {

    String ext = StringUtils.substring(fileName, 0,
        StringUtils.lastIndexOf(fileName, DOT) == -1 ? fileName.length() : StringUtils.lastIndexOf(fileName, DOT));
    return StringUtils.trimToEmpty(ext);
  }

  /**
   * ???????????????
   */
  public static String getExtension(String fileName) {

    if (StringUtils.INDEX_NOT_FOUND == StringUtils.indexOf(fileName, DOT)) {
      return StringUtils.EMPTY;
    }
    String ext = StringUtils.substring(fileName,
        StringUtils.lastIndexOf(fileName, DOT) + 1);
    return StringUtils.trimToEmpty(ext);
  }

  /**
   * ???????????????????????????
   */
  public static boolean isExtension(String fileName, String ext) {

    return StringUtils.equalsIgnoreCase(getExtension(fileName), ext);
  }

  /**
   * ???????????????????????????
   */
  public static boolean hasExtension(String fileName) {

    return !isExtension(fileName, StringUtils.EMPTY);
  }

  /**
   * ????????????????????????
   */
  public static String trimExtension(String ext) {

    return getExtension(DOT + ext);
  }

  /**
   * ???path??????????????????(???????????????????????????)
   */
  public static String fillExtension(String fileName, String ext) {

    fileName = replacePath(fileName + DOT);
    ext = trimExtension(ext);
    if (!hasExtension(fileName)) {
      return fileName + getExtension(ext);
    }
    if (!isExtension(fileName, ext)) {
      return getWithoutExtension(fileName) + getExtension(ext);
    }
    return fileName;
  }

  /**
   * ?????????????????????PATH
   */
  public static boolean isFile(String fileName) {

    return hasExtension(fileName);
  }

  /**
   * ????????????????????????PATH
   */
  public static boolean isFolder(String fileName) {

    return !hasExtension(fileName);
  }

  public static String replacePath(String path) {

    return StringUtils.replace(StringUtils.trimToEmpty(path), SLASH_ONE,
        SLASH_TWO);
  }

  /**
   * ??????PATH?????????
   */
  public static String trimLeftPath(String path) {

    if (isFile(path)) {
      return path;
    }
    path = replacePath(path);
    String top = StringUtils.left(path, 1);
    if (StringUtils.equalsIgnoreCase(SLASH_TWO, top)) {
      return StringUtils.substring(path, 1);
    }
    return path;
  }

  /**
   * ??????PATH?????????
   */
  public static String trimRightPath(String path) {

    if (isFile(path)) {
      return path;
    }
    path = replacePath(path);
    String bottom = StringUtils.right(path, 1);
    if (StringUtils.equalsIgnoreCase(SLASH_TWO, bottom)) {
      return StringUtils.substring(path, 0, path.length() - 2);
    }
    return path + SLASH_TWO;
  }

  /**
   * ??????PATH????????????????????????????????????PATH
   */
  public static String trimPath(String path) {

    path = StringUtils.replace(StringUtils.trimToEmpty(path), SLASH_ONE,
        SLASH_TWO);
    path = trimLeftPath(path);
    path = trimRightPath(path);
    return path;
  }

  /**
   * ????????????????????????PATH
   */
  public static String bulidFullPath(String... paths) {

    StringBuffer sb = new StringBuffer();
    for (String path : paths) {
      sb.append(trimPath(path));
    }
    return sb.toString();
  }

  /**
   * ?????????????????? path
   */
  public static String withoutHeadAndTailDiagonal(String path) {

    int start = 0;
    int end = 0;
    boolean existHeadDiagonal = path.startsWith(FileConstant.FILE_SEPARATORCHAR);
    boolean existTailDiagonal = path.endsWith(FileConstant.FILE_SEPARATORCHAR);
    if (existHeadDiagonal && existTailDiagonal) {
      start = StringUtils.indexOf(path, FileConstant.FILE_SEPARATORCHAR, 0) + 1;
      end = StringUtils.lastIndexOf(path, FileConstant.FILE_SEPARATORCHAR);
      return StringUtils.substring(path, start, end);
    } else if (existHeadDiagonal && !existTailDiagonal) {
      start = StringUtils.indexOf(path, FileConstant.FILE_SEPARATORCHAR, 0) + 1;
      return StringUtils.substring(path, start);
    } else if (!existHeadDiagonal && existTailDiagonal) {
      end = StringUtils.lastIndexOf(path, FileConstant.FILE_SEPARATORCHAR);
      return StringUtils.substring(path, 0, end);
    }
    return path;
  }

  public static void delFile(String filepath) {

    File f = new File(filepath);//??????????????????
    if (f.exists() && f.isDirectory()) {//???????????????????????????
      if (f.listFiles().length != 0) {
        //???????????????????????????????????????????????????????????????
        File delFile[] = f.listFiles();
        int i = f.listFiles().length;
        for (int j = 0; j < i; j++) {
          if (delFile[j].isDirectory()) {
            delFile(delFile[j].getAbsolutePath());//????????????del??????????????????????????????
          }
          delFile[j].delete();//????????????
        }
      }
      f.delete();
    }
  }

  public static void delFileList(List<String> filePaths) {

    for (String filePath : filePaths) {
      delFile(filePath);
    }
  }

  public static List<String> splitPath(String filePath) {

    List<String> pathList = new ArrayList<>();
    if (filePath.contains(FileConstant.FILE_SEPARATORCHAR)) {
      String[] arrPath = StringUtils.split(filePath, FileConstant.FILE_SEPARATORCHAR);
      StringBuilder sbPath = new StringBuilder();
      for (int i = 0; i < arrPath.length - 1; i++) {
        sbPath.append(FileConstant.FILE_SEPARATORCHAR).append(arrPath[i]);
        pathList.add(sbPath.toString());
      }

    }

    return pathList;
  }

  public static void main(String[] args) throws Exception {

  }

  public static String getParentPath(String filePath) {

    if (StringUtils.lastIndexOf(filePath, SLASH_ONE) == 0) {
      return SLASH_ONE;
    } else {
      String path = StringUtils.substring(filePath, 0,
          StringUtils.lastIndexOf(filePath, SLASH_ONE));
      return path;
    }
  }

  /**
   * ??????????????????????????????/?????????
   *
   * @param directoryPath ??????????????????????????????
   * @param isAddDirectory ??????????????????????????????????????????list?????????
   */
  public static List<String> getAllFile(String directoryPath, boolean isAddDirectory) {

    List<String> list = new ArrayList<String>();
    File baseFile = new File(directoryPath);
    if (baseFile.isFile() || !baseFile.exists()) {
      return list;
    }
    File[] files = baseFile.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        if (isAddDirectory) {
          list.add(file.getAbsolutePath());
        }
        list.addAll(getAllFile(file.getAbsolutePath(), isAddDirectory));
      } else {
        list.add(file.getAbsolutePath());
      }
    }
    return list;
  }



  public static void downloadFile(String name, String path, HttpServletRequest request,
      HttpServletResponse response) throws FileNotFoundException {
    File downloadFile = new File(path);
    String fileName = name;
    if (StringUtils.isBlank(fileName)) {
      fileName = downloadFile.getName();
    }
    String headerValue = String.format("attachment; filename=\"%s\"", fileName);
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION, headerValue);
    response.addHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
    //??????????????????
    long downloadSize = downloadFile.length();
    long fromPos = 0, toPos = 0;
    if (request.getHeader("Range") == null) {
      response.addHeader(HttpHeaders.CONTENT_LENGTH, downloadSize + "");
    } else {
      log.info("range:{}", response.getHeader("Range"));
      //?????????????????????
      response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
      String range = request.getHeader("Range");
      String bytes = range.replaceAll("bytes=", "");
      String[] ary = bytes.split("-");
      fromPos = Long.parseLong(ary[0]);
      log.info("fronPos:{}", fromPos);
      if (ary.length == 2) {
        toPos = Long.parseLong(ary[1]);
      }
      int size;
      if (toPos > fromPos) {
        size = (int) (toPos - fromPos);
      } else {
        size = (int) (downloadSize - fromPos);
      }
      response.addHeader(HttpHeaders.CONTENT_LENGTH, size + "");
      downloadSize = size;
    }

    try (RandomAccessFile in = new RandomAccessFile(downloadFile, "rw");
        OutputStream out = response.getOutputStream()) {
      if (fromPos > 0) {
        in.seek(fromPos);
      }
      int bufLen = (int) (downloadSize < 2048 ? downloadSize : 2048);
      byte[] buffer = new byte[bufLen];
      int num;
      //???????????????????????????
      int count = 0;
      while ((num = in.read(buffer)) != -1) {
        out.write(buffer, 0, num);
        count += num;
        if (downloadSize - count < bufLen) {
          bufLen = (int) (downloadSize - count);
          if (bufLen == 0) {
            break;
          }
          buffer = new byte[bufLen];
        }
      }
      response.flushBuffer();
    } catch (IOException e) {
      log.error("download error:" + e.getMessage(), e);
      throw new BizException("??????????????????", 406);
    }
  }


  /**
   * ???????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????? <a href="http://blog.163.com/wf_shunqiziran/blog/static/176307209201258102217810/">?????????????????????</a>
   */
  public static String charset(String path) {

    String charset = "GBK";
    byte[] first3Bytes = new byte[3];
    try {
      boolean checked = false;
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
      bis.mark(0); // ???????????? bis.mark(0);????????? bis.mark(100);????????????????????????????????????????????????????????????
      // Wagsn?????????????????????????????????????????????
      int read = bis.read(first3Bytes, 0, 3);
      if (read == -1) {
        bis.close();
        return charset; // ??????????????? ANSI
      } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
        charset = "UTF-16LE"; // ??????????????? Unicode
        checked = true;
      } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
        charset = "UTF-16BE"; // ??????????????? Unicode big endian
        checked = true;
      } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
          && first3Bytes[2] == (byte) 0xBF) {
        charset = "UTF-8"; // ??????????????? UTF-8
        checked = true;
      }
      bis.reset();
      if (!checked) {
        while ((read = bis.read()) != -1) {
          if (read >= 0xF0) {
            break;
          }
          if (0x80 <= read && read <= 0xBF) // ????????????BF?????????????????????GBK
          {
            break;
          }
          if (0xC0 <= read && read <= 0xDF) {
            read = bis.read();
            if (0x80 <= read && read <= 0xBF) // ????????? (0xC0 - 0xDF)
            // (0x80 - 0xBF),????????????GB?????????
            {
              continue;
            } else {
              break;
            }
          } else if (0xE0 <= read && read <= 0xEF) { // ???????????????????????????????????????
            read = bis.read();
            if (0x80 <= read && read <= 0xBF) {
              read = bis.read();
              if (0x80 <= read && read <= 0xBF) {
                charset = "UTF-8";
                break;
              } else {
                break;
              }
            } else {
              break;
            }
          }
        }
      }
      bis.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return charset;
  }

  public static boolean isBase64(String str) {

    String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    return Pattern.matches(base64Pattern, str);
  }

  @Value("${upload.window.root}")
  public void setUploadWindowRoot(String windowRoot) {

    uploadWindowRoot = windowRoot;
  }

  public static void close(final Closeable closeable){
    if(closeable != null){
      try {
        closeable.close();
      } catch (IOException e) {
        log.error("close fail:"+e.getMessage(),e);
      } finally {
      }
    }
  }

  /**
   * ???MappedByteBuffer???????????????????????????????????????????????????jvm crash???????????????????????????????????? ??????????????????????????????????????????????????????crash?????????????????????????????????????????????????????????????????? ?????????????????????????????????
   */
  public static void freedMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {

    try {
      if (mappedByteBuffer == null) {
        return;
      }

      mappedByteBuffer.force();
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {

          try {
            Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
            getCleanerMethod.setAccessible(true);
            Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(mappedByteBuffer,
                new Object[0]);
            cleaner.clean();
          } catch (Exception e) {
            log.error("clean MappedByteBuffer error!!!", e);
          }
          log.info("clean MappedByteBuffer completed!!!");
          return null;
        }
      });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void clean(final Object buffer) {

    AccessController.doPrivileged(new PrivilegedAction() {
      public Object run() {

        try {
          Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
          getCleanerMethod.setAccessible(true);
          Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
          cleaner.clean();
        } catch (Exception e) {
          log.error("clean fail :" + e.getMessage(), e);
        }
        return null;
      }
    });

  }

  public static void close(FileInputStream in, MappedByteBuffer byteBuffer) {

    if (null != in) {
      try {
        in.getChannel().close();
        in.close();
      } catch (IOException e) {
        log.error("close error:"+e.getMessage(), e);
      }
    }
    if (null != byteBuffer) {
      freedMappedByteBuffer(byteBuffer);
    }
  }
}
