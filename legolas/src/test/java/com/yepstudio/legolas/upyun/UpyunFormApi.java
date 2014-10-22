package com.yepstudio.legolas.upyun;

import java.io.File;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.Multipart;
import com.yepstudio.legolas.annotation.POST;
import com.yepstudio.legolas.annotation.Part;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

/**
 * 又拍云存储文件上传API
 * 
 * @author zzljob@gmail.com
 * @create 2014年10月22日
 * @version 1.0，2014年10月22日
 *
 */
@Api
public interface UpyunFormApi {

	@Description("上传文件")
	@POST("/{bucket}")
	@Multipart
	public Request upload(
			@Path("bucket") String bucket,
			@Part("policy") PolicyPart policy,
			@Part("signature") SignaturePart signature, 
			@Part("file") File file,
			OnRequestListener requestListener, 
			OnResponseListener<String> responseListener,
			OnErrorListener errorListener);

}
