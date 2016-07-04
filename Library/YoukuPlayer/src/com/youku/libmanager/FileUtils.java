package com.youku.libmanager;

import com.baseproject.utils.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;

public class FileUtils {

	public static final String TAG = "SoUpgradeService";
	
	/**
	 * 创建文件
	 * 
	 * @throws IOException
	 * */
	public static File creatFile(String fileName) throws IOException {
		
		if (fileName == null) return null;
		
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		return file;
	}

	/**
	 * 创建目录
	 * 
	 * @param dirName
	 * */
	public static File creatDir(String dirName) {
		
		if (dirName == null) return null;
		
		File dir = new File(dirName);
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}
		
		return dir;
	}

	/**
	 * 判断SD卡上的文件夹是否存在
	 * */
	public static boolean isFileExist(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}
	
	/** 
	 * 删除单个文件 
	 * @param   sPath    被删除文件的文件名 
	 * @return 单个文件删除成功返回true，否则返回false 
	 */  
	public static boolean deleteFile(String sPath) {  
		Logger.d(SoUpgradeService.TAG, "delete " + sPath);
		
	    Boolean flag = false;  
	    File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}  
	
	/** 
	 * 删除单个文件 
	 * @param   sPath    被删除文件的文件名 
	 * @return 单个文件删除成功返回true，否则返回false 
	 */  
	public static boolean deleteFile(File file) {  
	    Boolean flag = false;  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}
	
	/** 
	 * 删除目录（文件夹）以及目录下的文件 
	 * @param   sPath 被删除目录的文件路径 
	 * @return  目录删除成功返回true，否则返回false 
	 */  
	public static boolean deleteDirectory(String sPath) {  
		Boolean flag;
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //如果dir对应的文件不存在，或者不是一个目录，则退出  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    flag = true;  
	    //删除文件夹下的所有文件(包括子目录)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //删除子文件  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //删除子目录  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //删除当前目录  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}  

	
	public static boolean renameFile(String oldFilePath, String newFilePath) {
		
		if (oldFilePath == null || newFilePath == null)
			return false;
		
		File oldFile = new File(oldFilePath);
		File newFile = new File(newFilePath);
		
		return oldFile.renameTo(newFile);
	}
	
	/**
	 * 单个文件复制
	 * 
	 * @param sourceFile 源文件
	 * @param targetFile 目标文件
	 * @return 成功返回 0； 失败返回 1
	 */
	 public static int copyFile(File sourceFile, File targetFile) {
		 int flag = 0;
		 BufferedInputStream inBuff = null;
		 BufferedOutputStream outBuff = null;

		 if (!sourceFile.isFile()) {
			 Logger.e(TAG, sourceFile + " no exists!");
			 return -1;
		 }

		 try {
			 // 新建文件输入流并对它进行缓冲
			 inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			 // 新建文件输出流并对它进行缓冲
			 outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			 Logger.d(TAG, "copy " + sourceFile + " ------> " + targetFile);

			 Logger.d(TAG, sourceFile + " copy start");
			 // 缓冲数组
			 byte[] b = new byte[1024 * 5];
			 int len;
			 while ((len = inBuff.read(b)) != -1) {
				 outBuff.write(b, 0, len);
			 }
			 // 刷新此缓冲的输出流
			 outBuff.flush();

			 if (sourceFile.length() != targetFile.length()) {
				 Logger.e(TAG, sourceFile + " copy fail, file does not copy fully!");
				 Logger.d(TAG, sourceFile + " length : " + sourceFile.length());
				 Logger.d(TAG, targetFile + " length : " + targetFile.length());
				 deleteFile(targetFile);
				 flag = -1;
			 }
			 Logger.d(TAG, sourceFile + " copy finish!");

		 } catch (FileNotFoundException e) {
			 flag = -1;
			 Logger.e(TAG, e);
		 } catch (IOException e) {
			 flag = -1;
			 Logger.e(TAG, e);
		 } finally {
			 // 关闭流
			 try {
				 if (inBuff != null)
					 inBuff.close();
				 if (outBuff != null)
					 outBuff.close();
			 } catch (IOException e) {
				 Logger.e(TAG, e);
			 }

		 }

		 return flag;
	 }

	/**
	 * 将一个InputStream里面的数据写入到指定目录
	 * 
	 * @param path  存放文件的路径
	 * @param fileName  文件名称
	 * @param input 文件的输入流
	 * @param size 文件大小
	 * @return  写入文件成功 返回文件的句柄 
	 *          写入文件失败 null
	 */
	public static File writeFromInput(String path, String fileName,
			InputStream input, int size) {
		File file = null;
		OutputStream output = null;
		try {
			creatDir(path);
			file = creatFile(path + fileName);
			output = new FileOutputStream(file);
			
			byte buffer[] = new byte[1024];
			int length;
			int recvSize = 0;
			
			while ((length = (input.read(buffer))) > 0) {
				output.write(buffer, 0, length);
				recvSize += length;
				//Logger.d(TAG, "downloaded percent : " + ((float)recvSize / (float)size) * 100 + "%");
			}
			
			output.flush();
			
			if (file.length() != size) {
				Logger.d(TAG, "download fail");
				deleteFile(file);
				file = null;
				throw new Exception();
			} else {
				Logger.d(TAG, "download finish");
			}
		} catch (Exception e) {
			file = null;
			Logger.e(TAG, e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				Logger.e(TAG, e);
			}
		}
		return file;
	}

	public static String file2String(File file, String encoding) {
		if (file == null) {
			return null;
		}
		InputStreamReader reader = null;
		StringWriter writer = new StringWriter();
		try {
			if (encoding == null || "".equals(encoding.trim())) {
				reader = new InputStreamReader(new FileInputStream(file));
			} else {
				reader = new InputStreamReader(new FileInputStream(file), encoding);
			}
			//将输入流写入输出流
			char[] buffer = new char[1024];
			int n = 0;
			while (-1 != (n = reader.read(buffer))) {
				writer.write(buffer, 0, n);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		//返回转换结果
		if (writer != null)
			return writer.toString();
		else return null;
	}

}
