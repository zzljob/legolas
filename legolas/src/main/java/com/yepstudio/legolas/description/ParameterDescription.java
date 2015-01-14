package com.yepstudio.legolas.description;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.ParameterType;
import com.yepstudio.legolas.RequestType;
import com.yepstudio.legolas.annotation.Body;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.Field;
import com.yepstudio.legolas.annotation.Fields;
import com.yepstudio.legolas.annotation.Header;
import com.yepstudio.legolas.annotation.Headers;
import com.yepstudio.legolas.annotation.MuitiParameters;
import com.yepstudio.legolas.annotation.Part;
import com.yepstudio.legolas.annotation.Parts;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.annotation.Querys;
import com.yepstudio.legolas.exception.LegolasConfigureError;
import com.yepstudio.legolas.listener.LegolasListener;
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
	
	private static final String LOG_VALIDATE_NO_PARAMETERIZED_RESPONSELISTENER = "OnRequestListener must have Parameterized，like this OnRequestListener<String>";
	private static final String LOG_VALIDATE_NO_PARAMETERIZED_LEGOLASLISTENER = "LegolasListener must have Parameterized，like this LegolasListener<String, String>";
	public static final String LOG_VALIDATE_MUITI_PARAM = "@Path、@Header、@Headers、@Query、@Querys、@Field、@Fields、@Part 、@Parts、@Body  just only have one, ";
	private static final String LOG_VALIDATE_CLASS_PARAM_LIMIT = "class has @MuitiParameters, so it can not to be other param";
	private static final String LOG_VALIDATE_CLASS_PARAM_MUST_CLASS = "@MuitiParameters must be use Class";
	
	private final String description;
	private String name;
	private final ParameterType parameterType;

	private boolean muitiParameter = false;
	private final boolean classParameter;
	private final List<ParameterItemDescription> parameterItems;

	/**参数的Java Type**/
	private final Type type;
	private Type responseType;
	private Type errorType;

	private final boolean options;
	
	private final boolean requestListener;
	private final boolean responseListener;
	private final boolean errorListener;
	private final boolean legolasListener;
	
	public ParameterDescription(Parameter param) {
		type = param.getParameterizedType();
		
		Class<?> clazz = param.getType();
		MuitiParameters objParam = clazz.getAnnotation(MuitiParameters.class);
		classParameter = objParam != null;
		if (classParameter) {
			parameterItems = parseClassParameter(param);
		} else {
			parameterItems = null;
		}

		options = LegolasOptions.class.equals(clazz);
		requestListener = OnRequestListener.class.equals(clazz);
		responseListener = OnResponseListener.class.equals(clazz);
		errorListener = OnErrorListener.class.equals(clazz);
		legolasListener = LegolasListener.class.equals(clazz);
		
		if (responseListener) {
			if (type instanceof ParameterizedType) {
				responseType = getParameterUpperBound((ParameterizedType) type)[0];
			}
			validateResponseListener(responseType);
		}
		if (legolasListener) {
			if (type instanceof ParameterizedType) {
				Type[] types = getParameterUpperBound((ParameterizedType) type);
				responseType = types[0];
				errorType = types[1];
			}
			validateLegolasListener(responseType, errorType);
		}
		
		Description desp = param.getAnnotation(Description.class);
		this.description = desp == null ? "" : desp.value();
		
		this.parameterType = parseParameterType(param);
		if (this.parameterType != ParameterType.NONE && classParameter) {
			throw new LegolasConfigureError(LOG_VALIDATE_CLASS_PARAM_LIMIT);
		}
		
		//如果参数的注释@Path、@Header、@Query、@Field、@Part没有指定名称，那就使用参数的名字
		if (needName() && !hasName()) {
			throw new LegolasConfigureError("@Path、@Header、@Query、@Field、@Part need has no empty value ");
		}
		
		if (isIgnore()) {
			Legolas.getLog().w("param is ignore : " + param.getName());
		}
	}
	
	private List<ParameterItemDescription> parseClassParameter(Parameter param) {
		if (type == null) {
			return null;
		}
		Class<?> clazz = param.getType();
		if (clazz.isInterface() || clazz.isEnum()) {
			throw new LegolasConfigureError(LOG_VALIDATE_CLASS_PARAM_MUST_CLASS);
		}
		
		List<ParameterItemDescription> list = new LinkedList<ParameterItemDescription>();
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		Map<String, ParameterType> names = new HashMap<String, ParameterType>();
		if (fields != null) {
			for (java.lang.reflect.Field field : fields) {
				ParameterItemDescription item = new ParameterItemDescription(field);
				if (item.isIgnore()) {
					continue;
				}
				if (item.getParameterType() != names.get(item.getName())) {
					list.add(item);
					names.put(item.getName(), item.getParameterType());
				} else {
					throw new LegolasConfigureError("can not has > two same name and same ParameterType Field in" + clazz);
				}
			}
		}
		return list;
	}
	
	private boolean needName() {
		return !isMuitiParameter()
				&& (parameterType == ParameterType.HEADER
						|| parameterType == ParameterType.QUERY
						|| parameterType == ParameterType.PART
						|| parameterType == ParameterType.FIELD 
						|| parameterType == ParameterType.PATH);
	}
	
	private boolean hasName() {
		return name != null && !"".equals(name.trim());
	}
	
	private ParameterType parseParameterType(Parameter param) {
		ParameterType parameterType = ParameterType.NONE;
		Path path = param.getAnnotation(Path.class);
		if (path != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.PATH;
				muitiParameter = false;
				name = path.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Header header = param.getAnnotation(Header.class);
		if (header != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.HEADER;
				muitiParameter = false;
				name = header.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Headers headers = param.getAnnotation(Headers.class);
		if (headers != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.HEADER;
				muitiParameter = true;
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Query query = param.getAnnotation(Query.class);
		if (query != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.QUERY;
				muitiParameter = false;
				name = query.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Querys querys = param.getAnnotation(Querys.class);
		if (querys != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.QUERY;
				muitiParameter = true;
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Field field = param.getAnnotation(Field.class);
		if (field != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.FIELD;
				muitiParameter = false;
				name = field.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Fields fields = param.getAnnotation(Fields.class);
		if (fields != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.FIELD;
				muitiParameter = true;
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Part part = param.getAnnotation(Part.class);
		if (part != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.PART;
				muitiParameter = false;
				name = part.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Parts parts = param.getAnnotation(Parts.class);
		if (parts != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.PART;
				muitiParameter = true;
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}
		
		Body body = param.getAnnotation(Body.class);
		if (body != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.BODY;
				muitiParameter = false;
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}
		return parameterType;
	}
	
	private void validateResponseListener(Type responseType) {
		if (responseType == null) {
			throw new LegolasConfigureError(LOG_VALIDATE_NO_PARAMETERIZED_RESPONSELISTENER);
		}
	}
	
	private void validateLegolasListener(Type responseType, Type errorType) {
		if (responseType == null || errorType == null) {
			throw new LegolasConfigureError(LOG_VALIDATE_NO_PARAMETERIZED_LEGOLASLISTENER);
		} 
	}
	
	public boolean isListener() {
		return requestListener || responseListener || errorListener || legolasListener;
	}

	public boolean isOptions() {
		return options;
	}
	
	private static Type[] getParameterUpperBound(ParameterizedType type) {
		Type[] types = type.getActualTypeArguments();
		for (int i = 0; i < types.length; i++) {
			Type paramType = types[i];
			if (paramType instanceof WildcardType) {
				types[i] = ((WildcardType) paramType).getUpperBounds()[0];
			}
		}
		return types;
	}

	public boolean isIgnore() {
		return !isListener() && !isOptions() && parameterType == ParameterType.NONE && !isClassParameter();
	}
	
	public ParameterType getParameterType() {
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

	public boolean isMuitiParameter() {
		return muitiParameter;
	}

	public Type getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public boolean isClassParameter() {
		return classParameter;
	}

	public Type getErrorType() {
		return errorType;
	}

	public boolean isLegolasListener() {
		return legolasListener;
	}

	public List<ParameterItemDescription> getParameterItems() {
		return parameterItems;
	}
	
	public boolean supportRequestType(RequestType type) throws LegolasConfigureError {
		if (type == RequestType.SIMPLE) {
			if (parameterType == ParameterType.BODY
					|| parameterType == ParameterType.FIELD
					|| parameterType == ParameterType.PART) {
				throw new LegolasConfigureError("default RequestType is Simple, Simple request not support @Body、@Fields、@Field、@Parts、@Part, Please use @FormUrlEncoded or @Multipart");
			}
		} else if (type == RequestType.FORM_URL_ENCODED) {
			if (parameterType == ParameterType.PART) {
				throw new LegolasConfigureError("@FormUrlEncoded not support @Parts、@Part, Please use @Multipart");
			}
		} else if (type == RequestType.MULTIPART) {
			if (parameterType == ParameterType.FIELD) {
				throw new LegolasConfigureError("@Multipart not support @Fields、@Field, Please use @FormUrlEncoded");
			}
		}
		//如果是类参数的话，就看看下面有没有不支持的参数
		if (classParameter && parameterItems != null) {
			for (ParameterItemDescription parameterItemDescription : parameterItems) {
				if (!parameterItemDescription.supportRequestType(type)) {
					return false;
				}
			}
		}
		return true;
	}

}
