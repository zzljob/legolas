package com.yepstudio.legolas;

import java.util.List;

/**
 * 
 * @author zhanghongtao
 * 
 */
public class MessageInfo extends MessageApiBase {
	private static final long serialVersionUID = 3788362177117732330L;

	public Result Result;

	public static class Result {
		/** 记录总数 */
		public Integer TotalCount;
		public List<item> Messages;

		public static class item {
			public Integer ID;
			/** 标题 */
			public String Title;
			/** 摘要 */
			public String Summary;
			/** 内容链接 */
			public String Url;
			/** 发布时间 */
			public String UpdateTime;
			/** 是否已读 */
			public Boolean isRead;
		}
	}
}
