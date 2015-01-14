package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;

import android.os.Handler;
import android.os.Looper;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月9日
 * @version 1.0，2015年1月9日
 *
 */
public class AndroidUiThreadExecutor implements Executor {
	private Handler handler = new Handler(Looper.getMainLooper());

	@Override
	public void execute(Runnable command) {
		handler.post(command);
	}

}
