package com.yepstudio.legolas.webapi;

import com.yepstudio.legolas.MessageApiBase;
import com.yepstudio.legolas.MessageCount;
import com.yepstudio.legolas.MessageInfo;
import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

/**
 * 
 * @author zhanghongtao
 * 
 */
@Description("数米 消息中心 API")
@Api("/Message")
public interface MessageApi {

	@Description("获得消息未读数")
	@GET("GetUserUnreadMessageNumber")
	public void getUserUnreadMessageNumber(@Query("userid") Integer userid,
			@Query("startTime") String startTime, OnResponseListener<MessageCount> onResponse);

	@Description("获得消息信息")
	@GET("GetUserMessages")
	public void GetUserMessages(@Query("userid") Integer integer,
			@Query("pageIndex") int pageIndex, @Query("pageSize") int pageSize,
			@Query("startTime") String startTime, @Query("lastId") String lastId,
			OnRequestListener onRequest, OnErrorListener onError,
			OnResponseListener<MessageInfo> onResponse);

	@Description("提交已读消息")
	@GET("UserReadMessage")
	public void UserReadMessage(@Query("userid") Integer integer,
			@Query("messageids") String messageids, OnResponseListener<MessageApiBase> onResponse);

	@Description("提交已删除消息")
	@GET("UserDeleteMessages")
	public void UserDeleteMessages(@Query("userid") Integer integer,
			@Query("messageids") String messageids, OnResponseListener<MessageApiBase> onResponse);

}
