/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yepstudio.legolas.cache.disk;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.cache.CacheEntry;
import com.yepstudio.legolas.mime.ByteArrayResponseBody;
import com.yepstudio.legolas.mime.FileResponseBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * Cache implementation that caches files directly onto the hard disk in the specified
 * directory. The default disk usage size is 5MB, but is configurable.
 */
public class BasicDiskCache implements DiskCache {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
    /** Map of the Key, CacheHeader pairs */
    private final Map<String, CacheHeader> mEntries = new LinkedHashMap<String, CacheHeader>(16, .75f, true);

    /** Total amount of space currently used by the cache in bytes. */
    private long mTotalSize = 0;

    /** The root directory to use for the cache. */
    private final File mRootDirectory;

    /** The maximum size of the cache in bytes. */
    private final int mMaxCacheSizeInBytes;

    /** Default maximum disk usage in bytes. */
    private static final int DEFAULT_DISK_USAGE_BYTES = 1 * 100 * 1024 * 1024;

    /** High water mark percentage for the cache */
    private static final float HYSTERESIS_FACTOR = 0.9f;

    /** Magic number for current version of cache file format. */
    private static final int CACHE_MAGIC = 0x20120504;

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory.
     * @param rootDirectory The root directory of the cache.
     * @param maxCacheSizeInBytes The maximum size of the cache in bytes.
     */
    public BasicDiskCache(File rootDirectory, int maxCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mMaxCacheSizeInBytes = maxCacheSizeInBytes;
    }

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory using
     * the default maximum cache size of 5MB.
     * @param rootDirectory The root directory of the cache.
     */
    public BasicDiskCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }

    /**
     * Clears the cache. Deletes all cached files from disk.
     */
    @Override
    public synchronized void clear() {
        File[] files = mRootDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        mEntries.clear();
        mTotalSize = 0;
        Legolas.getLog().d("Cache cleared.");
    }
    
    /**
     * Returns the cache entry with the specified key if it exists, null otherwise.
     */
    @Override
    public synchronized CacheEntry<Response> get(String key) {
        CacheHeader entry = mEntries.get(key);
        // if the entry does not exist, return.
        if (entry == null) {
            return null;
        }

        File file = getFileForKey(key);
        File configFile = getConfigFileForFile(file);
        FileInputStream configInput = null;
        FileInputStream input = null;
        try {
        	configInput = new FileInputStream(configFile);
        	CacheHeader header = CacheHeader.readHeader(configInput);//load config
        	
        	Map<String, String> responseHeaders = header.responseHeaders;
        	String mimeType = "application/octet-stream";
			if (responseHeaders != null) {
				mimeType = responseHeaders.get(ResponseBody.Content_Type);
			}
        	
        	long length = 0;
			if (file != null && file.exists()) {
				length = file.length();
			}
			
			//计算MD5值
        	MessageDigest mdInst = MessageDigest.getInstance("MD5");
			
			//加载一个缓存文件
			ResponseBody body;
			input = new FileInputStream(file);
			if (length < ByteArrayResponseBody.MAX_LIMIT_SIZE) {
				byte[] data = readStreamToBytes(input, DEFAULT_BUFFER_SIZE);
				body = new ByteArrayResponseBody(mimeType, data);
				mdInst.update(data);
			} else {
				body = new FileResponseBody(mimeType, length, file);
				
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int read;
				while ((read = input.read(buffer)) != -1) {
					mdInst.update(buffer, 0, read);
				}
			}
			
			BigInteger bigInt = new BigInteger(1, mdInst.digest());
			StringBuilder hashtext = new StringBuilder(bigInt.toString(16));
			while (hashtext.length() < 32) {
				hashtext.insert(0, "0");
			}
			String md5 = hashtext.toString();
			if (header.MD5 == null || md5 == null || !md5.equalsIgnoreCase(header.MD5)) {
				// 文件被篡改或者损坏
				Legolas.getLog().d(String.format("%s: %s", file.getAbsolutePath(), "md5 is not same"));
				remove(key);
				return null;
			}
			
        	Response response = new Response(200, "OK", responseHeaders, body, false, true);
        	return new CacheEntry<Response>(response, responseHeaders, header.etag, header.serverDate, header.serverModified, header.softTtl, header.ttl);
        } catch (IOException e) {
        	Legolas.getLog().d(String.format("%s: %s", file.getAbsolutePath(), e.toString()));
            remove(key);
            return null;
        } catch (NoSuchAlgorithmException e) {
        	Legolas.getLog().d(String.format("%s: %s", file.getAbsolutePath(), e.toString()));
            remove(key);
            return null;
		} finally {
            if (configInput != null) {
                try {
                	configInput.close();
                } catch (IOException ioe) {
                }
            }
            if (input != null) {
            	try {
            		input.close();
            	} catch (IOException ioe) {
            	}
            }
        }
    }
    
    /**
     * Initializes the DiskBasedCache by scanning for all files currently in the
     * specified root directory. Creates the root directory if necessary.
     */
    @Override
    public synchronized void initialize() {
        if (!mRootDirectory.exists()) {
            if (!mRootDirectory.mkdirs()) {
            	Legolas.getLog().e(String.format("Unable to create cache dir %s", mRootDirectory.getAbsolutePath()));
            }
            return;
        }

        File[] files = mRootDirectory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
			if (!isConfigFile(file)) {
				continue;
			}
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                CacheHeader entry = CacheHeader.readHeader(fis);
                entry.size = file.length();
                putEntry(entry.key, entry);
            } catch (IOException e) {
                if (file != null) {
                   file.delete();
                }
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException ignored) { }
            }
        }
    }

    /**
     * Invalidates an entry in the cache.
     * @param key Cache key
     * @param fullExpire True to fully expire the entry, false to soft expire
     */
    @Override
    public synchronized void invalidate(String key, boolean fullExpire) {
    	CacheEntry<Response> entry = get(key);
        if (entry != null) {
            entry.makeExpired(fullExpire);
            put(key, entry);
        }

    }
    
	private boolean canCache(CacheEntry<Response> entry) {
		if (entry == null || entry.getData() == null) {
			return false;
		}
		return true;
		
//		ResponseBody body = entry.getData().getBody();
//		if (body instanceof ByteArrayResponseBody) {
//			return true;
//		} else if (body instanceof FileResponseBody) {
//			return true;
//		}
//		return false;
	}

    /**
     * Puts the entry with the specified key into the cache.
     */
    @Override
    public synchronized void put(String key, CacheEntry<Response> entry) {
		if (!canCache(entry)) {
			return;
		}
		ResponseBody body = entry.getData().getBody();
		Legolas.getLog().d("DiskCache put " + key);
		
		pruneIfNeeded((int) body.length());
        File file = getFileForKey(key);
        File configFile = getConfigFileForKey(key);
        FileOutputStream configFileOut = null;
        FileOutputStream fileOut = null;
        InputStream inputStream = null;
        try {
        	//计算MD5值
        	MessageDigest mdInst = MessageDigest.getInstance("MD5");
            
			// writeFile
        	inputStream = body.read();
			fileOut = new FileOutputStream(file, false);
			// 写流
			byte[] buffer = new byte[4096];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				fileOut.write(buffer, 0, read);
				mdInst.update(buffer, 0, read);
			}
			fileOut.flush();
			
			BigInteger bigInt = new BigInteger(1, mdInst.digest());
			StringBuilder hashtext = new StringBuilder(bigInt.toString(16));
			while (hashtext.length() < 32) {
				hashtext.insert(0, "0");
			}
			String md5 = hashtext.toString();
			
			//writeConfigFile
			configFileOut = new FileOutputStream(configFile, false);
			CacheHeader e = new CacheHeader(key, entry, md5);
			e.writeHeader(configFileOut);
			configFileOut.flush();
            
            putEntry(key, e);
            return;
        } catch (IOException e) {
		} catch (NoSuchAlgorithmException e1) {
		} finally {
			if (configFileOut != null) {
				try {
					configFileOut.close();
					configFileOut = null;
				} catch (IOException e) {
				}
			}
			if (fileOut != null) {
				try {
					fileOut.close();
					fileOut = null;
				} catch (IOException e) {
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (IOException e) {
				}
			}
			
		}
        boolean deleted = file.delete();
        if (!deleted) {
        	Legolas.getLog().d(String.format("Could not clean up file %s", file.getAbsolutePath()));
        }
    }

    /**
     * Removes the specified key from the cache if it exists.
     */
    @Override
    public synchronized void remove(String key) {
        boolean deleted = getFileForKey(key).delete();
        removeEntry(key);
        if (!deleted) {
        	Legolas.getLog().d(String.format("Could not delete cache entry for key=%s, filename=%s", key, getFilenameForKey(key)));
        }
    }

    /**
     * Creates a pseudo-unique filename for the specified cache key.
     * @param key The key to generate a file name for.
     * @return A pseudo-unique filename.
     */
    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    /**
     * Returns a file object for the given cache key.
     */
    public File getFileForKey(String key) {
        return new File(mRootDirectory, getFilenameForKey(key));
    }

    public File getConfigFileForKey(String key) {
		return new File(getFileForKey(key).getAbsolutePath() + ".config");
    }
    
    public File getConfigFileForFile(File file) {
		return new File(file.getAbsolutePath() + ".config");
    }

    public boolean isConfigFile(String fileName) {
    	return fileName != null && fileName.endsWith(".config");
    }
    
    public boolean isConfigFile(File file) {
    	return file != null && file.getName().endsWith(".config");
    }

    /**
     * Prunes the cache to fit the amount of bytes specified.
     * @param neededSpace The amount of bytes we are trying to fit into the cache.
     */
    private void pruneIfNeeded(int neededSpace) {
        if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes) {
            return;
        }
        Legolas.getLog().v("Pruning old cache entries.");

        long before = mTotalSize;
        int prunedFiles = 0;
        long startTime = System.currentTimeMillis();

        Iterator<Map.Entry<String, CacheHeader>> iterator = mEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheHeader> entry = iterator.next();
            CacheHeader e = entry.getValue();
            boolean deleted = getFileForKey(e.key).delete();
            if (deleted) {
                mTotalSize -= e.size;
            } else {
            	Legolas.getLog().d(String.format("Could not delete cache entry for key=%s, filename=%s", e.key, getFilenameForKey(e.key)));
            }
            iterator.remove();
            prunedFiles++;

            if ((mTotalSize + neededSpace) < mMaxCacheSizeInBytes * HYSTERESIS_FACTOR) {
                break;
            }
        }

        Legolas.getLog().v(String.format("pruned %d files, %d bytes, %d ms", prunedFiles, (mTotalSize - before), System.currentTimeMillis() - startTime));
    }

    /**
     * Puts the entry with the specified key into the cache.
     * @param key The key to identify the entry by.
     * @param entry The entry to cache.
     */
    private void putEntry(String key, CacheHeader entry) {
        if (!mEntries.containsKey(key)) {
            mTotalSize += entry.size;
        } else {
            CacheHeader oldEntry = mEntries.get(key);
            mTotalSize += (entry.size - oldEntry.size);
        }
        mEntries.put(key, entry);
    }

    /**
     * Removes the entry identified by 'key' from the cache.
     */
    private void removeEntry(String key) {
        CacheHeader entry = mEntries.get(key);
        if (entry != null) {
            mTotalSize -= entry.size;
            mEntries.remove(key);
        }
    }
    
    /**
     * Reads the contents of an InputStream into a byte[].
     * */
    private static byte[] streamToBytes(InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        int count;
        int pos = 0;
        while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
            pos += count;
        }
        if (pos != length) {
            throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
        }
        return bytes;
    }

    /**
     * Reads the contents of an InputStream into a byte[].
     * */
    private static byte[] readStreamToBytes(InputStream in, int bufferSize) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (in != null) {
			byte[] buf = new byte[bufferSize];
			int r;
			while ((r = in.read(buf)) != -1) {
				baos.write(buf, 0, r);
			}
		}
		return baos.toByteArray();
    }

    /**
     * Handles holding onto the cache headers for an entry.
     */
    // Visible for testing.
    static class CacheHeader {
        /** The size of the data identified by this CacheHeader. (This is not
         * serialized to disk. */
        public long size;

        /** The key that identifies the cache entry. */
        public String key;

        /** ETag for cache coherence. */
        public String etag;

        /** Date of this response as reported by the server. */
        public long serverDate;
        
        public long serverModified;

        /** TTL for this record. */
        public long ttl;

        /** Soft TTL for this record. */
        public long softTtl;

        /** Headers from the response resulting in this cache entry. */
        public Map<String, String> responseHeaders;
        
        public String MD5;

        private CacheHeader() { }

        /**
         * Instantiates a new CacheHeader object
         * @param key The key that identifies the cache entry
         * @param entry The cache entry.
         */
        public CacheHeader(String key, CacheEntry<Response> entry, String md5) {
            this.key = key;
			if (entry.getData() != null && entry.getData().getBody() != null) {
				this.size = entry.getData().getBody().length();
			} else {
				this.size = -1;
			}
            this.etag = entry.getEtag();
            this.serverDate = entry.getServerDate();
            this.serverModified = entry.getServerModified();
            this.ttl = entry.getTtl();
            this.softTtl = entry.getSoftTtl();
            this.responseHeaders = entry.getResponseHeaders();
            this.MD5 = md5;
        }

        /**
         * Reads the header off of an InputStream and returns a CacheHeader object.
         * @param is The InputStream to read from.
         * @throws IOException
         */
        public static CacheHeader readHeader(InputStream is) throws IOException {
            CacheHeader entry = new CacheHeader();
            int magic = readInt(is);
            if (magic != CACHE_MAGIC) {
                // don't bother deleting, it'll get pruned eventually
                throw new IOException();
            }
            entry.key = readString(is);
            entry.etag = readString(is);
            if ("".equals(entry.etag)) {
                entry.etag = null;
            }
            entry.serverDate = readLong(is);
            entry.serverModified = readLong(is);
            entry.ttl = readLong(is);
            entry.softTtl = readLong(is);
            entry.MD5 = readString(is);
			if ("".equals(entry.MD5)) {
				entry.MD5 = null;
			}
            entry.responseHeaders = readStringStringMap(is);
            return entry;
        }

        /**
         * Writes the contents of this CacheHeader to the specified OutputStream.
         */
		public void writeHeader(OutputStream os) throws IOException {
			writeInt(os, CACHE_MAGIC);
			writeString(os, key);
			writeString(os, etag == null ? "" : etag);
			writeLong(os, serverDate);
			writeLong(os, serverModified);
			writeLong(os, ttl);
			writeLong(os, softTtl);
			writeString(os, MD5);
			writeStringStringMap(responseHeaders, os);
			os.flush();
		}

    }

    /*
     * Homebrewed simple serialization system used for reading and writing cache
     * headers on disk. Once upon a time, this used the standard Java
     * Object{Input,Output}Stream, but the default implementation relies heavily
     * on reflection (even for standard types) and generates a ton of garbage.
     */

    /**
     * Simple wrapper around {@link InputStream#read()} that throws EOFException
     * instead of returning -1.
     */
    private static int read(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    static void writeInt(OutputStream os, int n) throws IOException {
        os.write((n >> 0) & 0xff);
        os.write((n >> 8) & 0xff);
        os.write((n >> 16) & 0xff);
        os.write((n >> 24) & 0xff);
    }

    static int readInt(InputStream is) throws IOException {
        int n = 0;
        n |= (read(is) << 0);
        n |= (read(is) << 8);
        n |= (read(is) << 16);
        n |= (read(is) << 24);
        return n;
    }

    static void writeLong(OutputStream os, long n) throws IOException {
        os.write((byte)(n >>> 0));
        os.write((byte)(n >>> 8));
        os.write((byte)(n >>> 16));
        os.write((byte)(n >>> 24));
        os.write((byte)(n >>> 32));
        os.write((byte)(n >>> 40));
        os.write((byte)(n >>> 48));
        os.write((byte)(n >>> 56));
    }

    static long readLong(InputStream is) throws IOException {
        long n = 0;
        n |= ((read(is) & 0xFFL) << 0);
        n |= ((read(is) & 0xFFL) << 8);
        n |= ((read(is) & 0xFFL) << 16);
        n |= ((read(is) & 0xFFL) << 24);
        n |= ((read(is) & 0xFFL) << 32);
        n |= ((read(is) & 0xFFL) << 40);
        n |= ((read(is) & 0xFFL) << 48);
        n |= ((read(is) & 0xFFL) << 56);
        return n;
    }

    static void writeString(OutputStream os, String s) throws IOException {
        byte[] b = s.getBytes("UTF-8");
        writeLong(os, b.length);
        os.write(b, 0, b.length);
    }

    static String readString(InputStream is) throws IOException {
        int n = (int) readLong(is);
        byte[] b = streamToBytes(is, n);
        return new String(b, "UTF-8");
    }

    static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
        if (map != null) {
            writeInt(os, map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writeString(os, entry.getKey() == null ? "" : entry.getKey());
                writeString(os, entry.getValue() == null ? "" : entry.getValue());
            }
        } else {
            writeInt(os, 0);
        }
    }

    static Map<String, String> readStringStringMap(InputStream is) throws IOException {
        int size = readInt(is);
        Map<String, String> result = (size == 0)
                ? Collections.<String, String>emptyMap()
                : new HashMap<String, String>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(is).intern();
            String value = readString(is).intern();
            result.put(key, value);
        }
        return result;
    }

	@Override
	public void close() {
		
	}


}
