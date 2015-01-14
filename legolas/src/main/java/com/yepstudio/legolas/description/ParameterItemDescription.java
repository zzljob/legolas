package com.yepstudio.legolas.description;

import java.lang.reflect.Type;

import com.yepstudio.legolas.ParameterType;
import com.yepstudio.legolas.RequestType;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.Field;
import com.yepstudio.legolas.annotation.Header;
import com.yepstudio.legolas.annotation.Part;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.exception.LegolasConfigureError;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年11月6日
 * @version 1.0，2014年11月6日
 *
 */
public class ParameterItemDescription {

	private static final String LOG_VALIDATE_MUITI_PARAM = ParameterDescription.LOG_VALIDATE_MUITI_PARAM;

	private final String description;
	private String name;
	private java.lang.reflect.Field field;
	private final ParameterType parameterType;
	private final boolean ignore;

	public ParameterItemDescription(java.lang.reflect.Field fieldObj) {
		super();
		this.field = fieldObj;
		this.parameterType = parseParameterType(fieldObj);

		ignore = parameterType == ParameterType.NONE;

		if (!ignore && (name == null || "".equals(name.trim()))) {
			name = fieldObj.getName();
		}

		Description d = fieldObj.getAnnotation(Description.class);
		description = d == null ? null : d.value();
	}

	private ParameterType parseParameterType(java.lang.reflect.Field param) {
		ParameterType parameterType = ParameterType.NONE;

		Header header = param.getAnnotation(Header.class);
		if (header != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.HEADER;
				name = header.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Query query = param.getAnnotation(Query.class);
		if (query != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.QUERY;
				name = query.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Field field = param.getAnnotation(Field.class);
		if (field != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.FIELD;
				name = field.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		Part part = param.getAnnotation(Part.class);
		if (part != null) {
			if (parameterType == ParameterType.NONE) {
				parameterType = ParameterType.PART;
				name = part.value();
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_MUITI_PARAM);
			}
		}

		return parameterType;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isIgnore() {
		return ignore;
	}
	
	public boolean supportRequestType(RequestType type) {
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
		return true;
	}

	public Type getValueType() {
		return field.getGenericType();
	}
	
	public Object getValue(Object obj) {
		if (obj == null || field == null) {
			return null;
		}
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			return field.get(obj);
		} catch (IllegalArgumentException e) {

		} catch (IllegalAccessException e) {

		}
		return null;
	}

	public ParameterType getParameterType() {
		return parameterType;
	}

}
