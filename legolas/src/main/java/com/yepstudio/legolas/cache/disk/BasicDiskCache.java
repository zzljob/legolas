package com.yepstudio.legolas.cache.disk;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

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
				if (!file.delete()) {
					file.deleteOnExit();
				}
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
        try {
        	configInput = new FileInputStream(configFile);
        	CacheHeader header = CacheHeader.readHeader(configInput);//load config
        	closeStream(configInput);
        	
        	Map<String, String> responseHeaders = header.responseHeaders;
        	String mimeType = "application/octet-stream";
			if (responseHeaders != null) {
				mimeType = responseHeaders.get(ResponseBody.Content_Type);
			}
			Checksum checksum = createChecksum();
			ResponseBody body = readFileToResponseBody(mimeType, file, DEFAULT_BUFFER_SIZE, checksum);
			String sumValue = checksumToString(checksum);
			if (header.checksum == null || sumValue == null || !sumValue.equalsIgnoreCase(header.checksum)) {
				// 文件被篡改或者损坏
				Legolas.getLog().d(String.format("%s: %s", file.getAbsolutePath(), "checksum is not same"));
				remove(key);
				return null;
			}
        	Response response = new Response(200, "OK", responseHeaders, body, false, true);
        	return new CacheEntry<Response>(response, responseHeaders, header.etag, header.serverDate, header.serverModified, header.softTtl, header.ttl);
        } catch (IOException e) {
        	Legolas.getLog().d(String.format("%s: %s", file.getAbsolutePath(), e.toString()));
            remove(key);
            return null;
        } finally {
        	closeStream(configInput);
        }
    }
    
	protected ResponseBody readFileToResponseBody(String mimeType, File file, int bufferSize, Checksum checksum) throws IOException {
		long length = 0;
		if (file != null && file.exists()) {
			length = file.length();
		}
		//加载一个缓存文件
		ResponseBody body;
		if (length < ByteArrayResponseBody.MAX_LIMIT_SIZE) {
			byte[] data = readFileToBytes(file, DEFAULT_BUFFER_SIZE, checksum);
			body = new ByteArrayResponseBody(mimeType, data);
		} else {
			InputStream stream = readFileToStream(file, DEFAULT_BUFFER_SIZE, checksum);
			body = new FileResponseBody(mimeType, length, file, stream);
		}
		return body;
	}
	
	static void doChecksum(File file, int bufferSize, Checksum checksum) throws IOException {
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			byte[] buffer = new byte[bufferSize];
			int read;
			while ((read = input.read(buffer)) != -1) {
				checksum.update(buffer, 0, read);
			}
		} finally {
			closeStream(input);
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
        Set<File> useable = new HashSet<File>();
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
                useable.add(file);
            } catch (IOException e) {
                if (file != null) {
                   file.delete();
                }
            } finally {
            	closeStream(fis);
            }
        }
        //清除一些不可用的文件
        for (File file : files) {
        	if (!isConfigFile(file)) {
				continue;
			}
			if (!useable.contains(file)) {
				if (!file.delete()) {
					file.deleteOnExit();
				}
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
	}
	
	static void closeStream(InputStream input) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException ignored) { }
		}
	}
	
	static void closeStream(OutputStream output) {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
			}
		}
	}
	
	protected void writeStream(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read;
		while ((read = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}
		outputStream.flush();
	}
	
	protected Checksum createChecksum() {
		return new CRC32();
	}
	
	protected String checksumToString(Checksum sum) {
//		BigInteger bigInt = new BigInteger(1, mdInst.digest());
//		StringBuilder hashtext = new StringBuilder(bigInt.toString(16));
//		while (hashtext.length() < 32) {
//			hashtext.insert(0, "0");
//		}
		return sum.getValue() + "";
	} 
	
	protected Checksum writeResponseBodyToFile(ResponseBody body, File file, int bufferSize) throws IOException {
		BufferedOutputStream bufferedOut = null;
		FileOutputStream fileOut = null;
		InputStream input = null;
		CheckedInputStream checkedInput = null;  
		try {
			if (body instanceof FileResponseBody) {
				input = new FileInputStream(((FileResponseBody) body).getFile());
			} else {
				input = body.read();
			}
			checkedInput = new CheckedInputStream(input, createChecksum());
			fileOut = new FileOutputStream(file, false);
			bufferedOut = new BufferedOutputStream(fileOut);
			writeStream(checkedInput, bufferedOut, bufferSize);
			bufferedOut.flush();
			return checkedInput.getChecksum();
		} finally {
			closeStream(bufferedOut);
			closeStream(fileOut);
			closeStream(input);
			closeStream(checkedInput);
		}
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
        try {
			// 写流
			//如果是需要加密，直接在方法writeStream加密就好了
        	Checksum checksum = writeResponseBodyToFile(body, file, DEFAULT_BUFFER_SIZE);
        	String sumValue =  checksumToString(checksum);
        			
			//writeConfigFile
			configFileOut = new FileOutputStream(configFile, false);
			CacheHeader e = new CacheHeader(key, entry, sumValue);
			e.writeHeader(configFileOut);
			configFileOut.flush();
            
            putEntry(key, e);
            return;
        } catch (IOException e) {
        	Legolas.getLog().w("save cache file fail", e);
		} finally {
			closeStream(configFileOut);
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
        return localFilename + getFileSuffix();
    }
    
    protected String getFileSuffix() {
		return "";
	}

    /**
     * Returns a file object for the given cache key.
     */
    public File getFileForKey(String key) {
        return new File(mRootDirectory, getFilenameForKey(key));
    }
    
	protected String getConfigFileSuffix() {
		return ".config";
	}

    public File getConfigFileForKey(String key) {
		return new File(getFileForKey(key).getAbsolutePath() + getConfigFileSuffix());
    }
    
    public File getConfigFileForFile(File file) {
		return new File(file.getAbsolutePath() + getConfigFileSuffix());
    }

    public boolean isConfigFile(String fileName) {
    	return fileName != null && fileName.endsWith(getConfigFileSuffix());
    }
    
    public boolean isConfigFile(File file) {
    	return file != null && file.getName().endsWith(getConfigFileSuffix());
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
            File file = getFileForKey(e.key);
            boolean deleted = file.delete();
            if (deleted) {
                mTotalSize -= e.size;
            } else {
            	file.deleteOnExit();
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
	protected byte[] readFileToBytes(File file, int bufferSize, Checksum checksum) throws IOException {
		FileInputStream input = null;
		CheckedInputStream checkedInput = null;
		try {
			input = new FileInputStream(file);
			checkedInput = new CheckedInputStream(input, checksum);
			byte[] data = readStreamToBytes(checkedInput, bufferSize);
			return data;
		} finally {
			closeStream(input);
			closeStream(checkedInput);
		}
	}
	
	protected byte[] readStreamToBytes(InputStream stream, int bufferSize) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[bufferSize];
		int r;
		while ((r = stream.read(buf)) != -1) {
			baos.write(buf, 0, r);
		}
		return baos.toByteArray();
	}
    
    protected InputStream readFileToStream(File file, int bufferSize, Checksum checksum) throws IOException {
    	doChecksum(file, bufferSize, checksum);
    	return new FileInputStream(file);
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
        
        public String checksum;

        private CacheHeader() { }

        /**
         * Instantiates a new CacheHeader object
         * @param key The key that identifies the cache entry
         * @param entry The cache entry.
         */
        public CacheHeader(String key, CacheEntry<Response> entry, String checksum) {
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
            this.checksum = checksum;
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
            entry.checksum = readString(is);
			if ("".equals(entry.checksum)) {
				entry.checksum = null;
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
			writeString(os, checksum);
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
		mEntries.clear();
	}


}
