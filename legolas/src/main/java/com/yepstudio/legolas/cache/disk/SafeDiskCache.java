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

public class SafeDiskCache extends BasicDiskCache {
	
	private static String Algorithm = "DESede"; // 定义 加密算法,可用DES,DESede,Blowfish
	private Cipher encryptCipher;//加密的
	private Cipher decryptCipher;//解密的
	private byte[] key = null;
	
	public SafeDiskCache(File rootDirectory, int maxCacheSizeInBytes, byte[] key) {
		super(rootDirectory, maxCacheSizeInBytes);
		this.key = key;
	}

	public SafeDiskCache(File rootDirectory, byte[] key) {
		super(rootDirectory);
		this.key = key;
	}
	
	public SafeDiskCache(File rootDirectory) {
		super(rootDirectory);
	}
	
	@Override
	public synchronized void initialize() {
		super.initialize();
		try {
			SecretKey deskey = null;
			if (key != null) {
				deskey = new SecretKeySpec(key, Algorithm);
			} else {
				KeyGenerator keygen = KeyGenerator.getInstance(Algorithm);
				keygen.init(168);
				deskey = keygen.generateKey();
			}
			
			encryptCipher = Cipher.getInstance(Algorithm);
			encryptCipher.init(Cipher.ENCRYPT_MODE, deskey);

			decryptCipher = Cipher.getInstance(Algorithm);
			decryptCipher.init(Cipher.DECRYPT_MODE, deskey);
		} catch (Exception e) {
			Legolas.getLog().e("initialize", e);
			throw new IllegalStateException("key is error ", e);
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
