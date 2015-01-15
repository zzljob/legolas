package com.yepstudio.legolas.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.Profiler;
import com.yepstudio.legolas.ProfilerDelivery;
import com.yepstudio.legolas.request.BasicRequest;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public class ExecutorProfilerDelivery implements ProfilerDelivery {
	
	private final Executor deliveryExecutor = Executors.newSingleThreadExecutor();
	private final Executor taskExecutor;
	private final Profiler<?> profiler;
	private boolean enableProfiler;

	public ExecutorProfilerDelivery(Executor taskExecutor, Profiler<?> profiler) {
		super();
		this.profiler = profiler;
		this.taskExecutor = taskExecutor;
	}

	@Override
	public void postRequestStart(final BasicRequest wrapper) {
		if (!enableProfiler) {
			return ;
		}
		Legolas.getLog().d("deliveryExecutor submit Runnable");
		deliveryExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				Legolas.getLog().d("taskExecutor execute ProfilerRunnable");
				ProfilerRunnable run = new ProfilerRunnable(profiler, wrapper, null, null);
				run.isBeforeCall = true;
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				//任务线程去执行任务
				taskExecutor.execute(task);
				try {
					//等待任务线程执行完成
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		});
	}

	@Override
	public void postRequestEnd(final BasicRequest wrapper, final Response response, final LegolasException exception) {
		if (!enableProfiler) {
			return ;
		}
		Legolas.getLog().d("deliveryExecutor submit Runnable");
		deliveryExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				Legolas.getLog().d("taskExecutor execute ProfilerRunnable");
				ProfilerRunnable run = new ProfilerRunnable(profiler, wrapper, response, exception);
				run.isEndCall = true;
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		});
	}

	@Override
	public void postRequestCancel(final BasicRequest wrapper, final Response response) {
		if (!enableProfiler) {
			return ;
		}
		Legolas.getLog().d("deliveryExecutor submit Runnable");
		deliveryExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				Legolas.getLog().d("taskExecutor execute ProfilerRunnable");
				ProfilerRunnable run = new ProfilerRunnable(profiler, wrapper, response, null);
				run.isCancleCall = true;
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		});
	}

	@Override
	public void enableProfiler(boolean enable) {
		this.enableProfiler = enable;
	}
	
	@SuppressWarnings("rawtypes")
	private static class ProfilerRunnable implements Runnable {

		final Profiler profiler;
		final BasicRequest wrapper;
		final Response response;
		final LegolasException exception;
		boolean isBeforeCall = false;
		boolean isEndCall = false;
		boolean isCancleCall = false;

		public ProfilerRunnable(Profiler profiler, BasicRequest wrapper, Response response, LegolasException exception) {
			super();
			this.profiler = profiler;
			this.wrapper = wrapper;
			this.response = response;
			this.exception = exception;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (isBeforeCall) {
				Legolas.getLog().d("profiler beforeCall");
				Object beforeCallData = profiler.beforeCall(wrapper);
				wrapper.setProfilerExpansion(beforeCallData);
			}
			if (isEndCall) {
				Legolas.getLog().d("profiler afterCall");
				Object beforeCallData = wrapper.getProfilerExpansion();
				profiler.afterCall(response, exception, beforeCallData);
			}
			if (isCancleCall) {
				Legolas.getLog().d("profiler cancelCall");
				Object beforeCallData = wrapper.getProfilerExpansion();
				profiler.cancelCall(beforeCallData);
			}
		}

	}

}
