package com.yepstudio.legolas.description;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.TypesHelper;
import com.yepstudio.legolas.annotation.Body;
import com.yepstudio.legolas.annotation.Field;
import com.yepstudio.legolas.annotation.Header;
import com.yepstudio.legolas.annotation.Part;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月23日
 * @version 2.0, 2014年4月23日
 * 
 */
public class ParameterDescription {

	private static LegolasLog log = LegolasLog.getClazz(ParameterDescription.class);

	private final Type type;
	private final Annotation[] annotations;

	private int parameterType = ParameterType.NONE;
	private String name;

	private boolean response;
	private Type responseType;

	private boolean requestListener = false;
	private boolean responseListener = false;
	private boolean errorListener = false;

	private boolean options = false;

	public static interface ParameterType {
		final int PATH = 0;
		final int QUERY = 1;
		
		final int FIELD = 2;
		final int PART = 3;
		final int BODY = 4;
		
		final int HEADER = 5;
		final int OPTIONS = 6;
		final int NONE = -1;
	}

	public ParameterDescription(Type type, Annotation... annotations) {
		super();
		this.type = type;
		this.annotations = annotations;
		parseParamAnnotation();
		parseParamsWithoutAnnotation();
	}

	private void parseParamAnnotation() {
		if (annotations == null || annotations.length <= 0) {
			return;
		}
		log.v("parseAnnotation:");
		for (Annotation annotation : annotations) {
			if (Path.class == annotation.annotationType()) {
				parameterType = ParameterType.PATH;
				Path anno = (Path) annotation;
				name = anno.value();
			} else if (Header.class == annotation.annotationType()) {
				parameterType = ParameterType.HEADER;
				Header anno = (Header) annotation;
				name = anno.value();
			} else if (Query.class == annotation.annotationType()) {
				parameterType = ParameterType.QUERY;
				Query anno = (Query) annotation;
				name = anno.value();
			} else if (Part.class == annotation.annotationType()) {
				parameterType = ParameterType.PART;
				Part anno = (Part) annotation;
				name = anno.value();
			} else if (Field.class == annotation.annotationType()) {
				parameterType = ParameterType.FIELD;
				Field anno = (Field) annotation;
				name = anno.value();
			} else if (Body.class == annotation.annotationType()) {
				parameterType = ParameterType.BODY;
				Body anno = (Body) annotation;
				name = anno.value();
			} else {
				
			}
			log.v(annotation.toString());
			if (name == null || name.length() < 1) {
				throw new IllegalStateException("parameter with Annotation, name can not be empty.");
			}
		}
	}

	private void parseParamsWithoutAnnotation() {
		if (annotations != null && annotations.length > 0) {
			return;
		}
		log.v("start parseParamsWithoutAnnotation...");
		if (type instanceof Class<?>) {
			if (OnRequestListener.class.equals(type)) {
				requestListener = true;
				log.v("type:" + type + ", is OnRequestListener");
			} else if (OnErrorListener.class.equals(type)) {
				errorListener = true;
				log.v("type:" + type + ", is OnErrorListener");
			} else if (LegolasOptions.class.equals(type)) {
				parameterType = ParameterType.OPTIONS;
				options = true;
				log.v("type:" + type + ", is LegolasOptions");
			}
		} else if (type instanceof ParameterizedType) {
			Class<?> clazz = TypesHelper.getRawType(type);
			if (OnResponseListener.class.equals(clazz)) {
				responseType = getParameterUpperBound((ParameterizedType) type);
				response = true;
				responseListener = true;
				log.v("type:" + type + ", is OnResponseListener, and responseType:[" + responseType + "]");
			}
		}

	}

	private static Type getParameterUpperBound(ParameterizedType type) {
		Type[] types = type.getActualTypeArguments();
		for (int i = 0; i < types.length; i++) {
			Type paramType = types[i];
			if (paramType instanceof WildcardType) {
				types[i] = ((WildcardType) paramType).getUpperBounds()[0];
			}
		}
		log.v("getParameterUpperBound:" + types[0]);
		return types[0];
	}

	public boolean isIgnore() {
		return !(isRequestParameter() || isListener() || isOptions());
	}
	
	public boolean isRequestParameter() {
		return parameterType != ParameterType.NONE;
	}
	
	public boolean isListener() {
		return requestListener || responseListener || errorListener;
	}

	public boolean isResponse() {
		return response;
	}
	
	public boolean isOptions() {
		return options;
	}

	public int getParameterType() {
		return parameterType;
	}

	public String getName() {
		return name;
	}

	public Type getResponseType() {
		return responseType;
	}

	public boolean isRequestListener() {
		return requestListener;
	}

	public boolean isResponseListener() {
		return responseListener;
	}

	public boolean isErrorListener() {
		return errorListener;
	}

}
