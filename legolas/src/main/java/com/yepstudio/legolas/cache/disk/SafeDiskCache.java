package com.yepstudio.legolas.cache.disk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Checksum;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.yepstudio.legolas.Legolas;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月19日
 * @version 1.0，2015年1月19日
 *
 */
public class SafeDiskCache extends BasicDiskCache {
	
	private static final int DEFAULT_DISK_USAGE_BYTES = 1 * 100 * 1024 * 1024;
	
	/** 定义 加密算法, 可用DES, DESede, Blowfish **/
	private static String ALGORITHM = "DESede";
	/**
	 * <p>
	 * 定义 加密/解密算法/工作模式/填充方式
	 * </p>
	 * 需要注意不同的语言之间的工作模式和填充模式都可能会不一样<br/>
	 * 同一种语言不同的平台也可能工作模式和填充模式都可能会不一样<br/>
	 * <p>
	 * DES一共有四种模式：电子密码本模式（ECB）、加密分组链接模式（CBC）、加密反馈模式（CFB）和输出反馈模式（OFB）<br/>
	 * 填充模式：NoPadding算法本身不对数据进行处理，加密数据由加密双方约定填补算法；PKCS5Padding加密前数据字节长度对8取余
	 * </p>
	 **/
	private static String CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding";
	
	
	private Cipher encryptCipher;//加密的
	private Cipher decryptCipher;//解密的
	
	public SafeDiskCache(File rootDirectory, int maxCacheSizeInBytes, Cipher encryptCipher, Cipher decryptCipher) {
		super(rootDirectory, maxCacheSizeInBytes);
		this.encryptCipher = encryptCipher;
		this.decryptCipher = decryptCipher;
	}

	public SafeDiskCache(File rootDirectory, Cipher encryptCipher, Cipher decryptCipher) {
		this(rootDirectory, DEFAULT_DISK_USAGE_BYTES, encryptCipher, decryptCipher);
	}
	
	public SafeDiskCache(File rootDirectory, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		this(rootDirectory, null, null);
		initCipherByDefaultAlgorithm(key);
	}
	
	public SafeDiskCache(File rootDirectory) {
		this(rootDirectory, null, null);
	}
	
	@Override
	public synchronized void initialize() {
		super.initialize();
		if (encryptCipher == null || decryptCipher == null) {
			useDefaultKeyAndAlgorithm();
		}
	}
	
	public void initCipherByDefaultAlgorithm(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		SecretKey deskey = new SecretKeySpec(key, ALGORITHM);
		
		encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
		encryptCipher.init(Cipher.ENCRYPT_MODE, deskey);
		
		decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
		decryptCipher.init(Cipher.DECRYPT_MODE, deskey);
	}
	
	public byte[] useDefaultKeyAndAlgorithm() {
		SecretKey deskey = null;
		try{
			KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
			keygen.init(168);
			deskey = keygen.generateKey();
			
			encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
			encryptCipher.init(Cipher.ENCRYPT_MODE, deskey);
			
			decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
			decryptCipher.init(Cipher.DECRYPT_MODE, deskey);
			
			return deskey.getEncoded();
		} catch (Exception e) {
			throw new IllegalStateException("key is can not be init ", e);
		}
	}
	
	
	/**
	 * 获取解密的密钥
	 * @return
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	protected Cipher getDecryptCipher() {
		return decryptCipher;
	}
	
	/***
	 * 获取加密的密钥
	 * @return
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	protected Cipher getEncryptCipher() {
		return encryptCipher;
	}
	
	protected void writeStream(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
		CipherInputStream cipherIntput = null;//加密的
		byte[] buffer = new byte[bufferSize];
		int read;

		try {
			cipherIntput = new CipherInputStream(inputStream, getEncryptCipher());
			while ((read = cipherIntput.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}
			outputStream.flush();
			
		} catch (Exception e) {
			Legolas.getLog().e("writeStream getEncryptCipher", e);
			
			while ((read = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}
			outputStream.flush();
		} finally {
			closeStream(cipherIntput);
		}
		
	}
	
	@Override
	protected byte[] readFileToBytes(File file, int bufferSize, Checksum checksum) throws IOException {
		FileInputStream input = null;
		CipherInputStream cipherInput = null;
		
		ByteArrayOutputStream byteOutput = null;
		try {
			input = new FileInputStream(file);
			byteOutput = new ByteArrayOutputStream();
			
			byte[] buf = new byte[bufferSize];
			int r;
			try {
				cipherInput = new CipherInputStream(input, getDecryptCipher());
				while ((r = cipherInput.read(buf)) != -1) {
					byteOutput.write(buf, 0, r);
				}
			} catch (Exception e) {
				while ((r = input.read(buf)) != -1) {
					byteOutput.write(buf, 0, r);
				}
			}
			
			byte[] data = byteOutput.toByteArray();
			checksum.update(data, 0, data.length);
			return data;
		} finally {
			closeStream(input);
			closeStream(cipherInput);
			closeStream(byteOutput);
		}
	}

	@Override
	protected InputStream readFileToStream(File file, int bufferSize, Checksum checksum) throws IOException {
		FileInputStream input = new FileInputStream(file);
		try {
			return new CipherInputStream(input, getDecryptCipher());
		} catch (Exception e) {
			Legolas.getLog().e("writeStream getEncryptCipher", e);
		}
		return input;
	}

	@Override
	protected String getConfigFileSuffix() {
		return ".safec";
	}
	
	protected String getFileSuffix() {
		return ".safe";
	}

}
