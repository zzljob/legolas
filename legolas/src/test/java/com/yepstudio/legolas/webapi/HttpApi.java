package com.yepstudio.legolas.webapi;

import com.google.gson.annotations.SerializedName;
import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.Field;
import com.yepstudio.legolas.annotation.FormUrlEncoded;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.MuitiParameters;
import com.yepstudio.legolas.annotation.POST;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.request.Request;

@Description("数米金融数据API")
@Api("/News")
public interface HttpApi {

	@Description("获得新闻标题")
	@GET("NewsTitle.ashx")
	public String syncGetNewsTitle(
			@Description("版本") @Query("version") double version,
			@Description("格式化") @Query("format") String format,
			@Description("新闻类型") @Query("newstype") int newstype,
			@Description("分页页数") @Query("pageno") int pageno,
			@Description("从第几条数据开始") @Query("applyrecordno") int applyrecordno);

	@Description("获得新闻标题多参数")
	@POST("NewsTitle.ashx")
	@FormUrlEncoded
	public NewsTitleEntity syncGetNewsTitleDto(NewsTitleDTO dto);

	public static class NewsTitleEntity {
		@SerializedName("errcode")
		private int errcode;
		@SerializedName("errmsg")
		private String errmsg;
		@SerializedName("totalrecords")
		private int totalrecords;
		@SerializedName("datatable")
		private String datatable;
	}

	@MuitiParameters
	public static class NewsTitleDTO {
		@Description("版本")
		@Field("version")
		double version;
		@Description("格式化")
		@Query("format")
		String format;
		@Description("新闻类型")
		@Query("newstype")
		int newstype;
		@Description("分页页数")
		@Query("pageno")
		int pageno;
		@Description("从第几条数据开始")
		@Query("applyrecordno")
		int applyrecordno;
		int xxx;
	}

	@Description("获得新闻标题")
	@GET("NewsTitle.ashx")
	public Request asyncNewsTitle(
			@Description("版本") @Query("version") double version,
			@Description("格式化") @Query("format") String format,
			@Description("新闻类型") @Query("newstype") int newstype,
			@Description("分页页数") @Query("pageno") int pageno,
			@Description("从第几条数据开始") @Query("applyrecordno") int applyrecordno,
			LegolasListener<String, String> listener);

}
