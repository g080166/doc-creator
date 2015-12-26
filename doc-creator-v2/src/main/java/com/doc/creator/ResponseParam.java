package com.doc.creator;

import java.util.List;

public class ResponseParam {
	String name;
	String type;
	String desc;
	String defaultValue;
	String realType;
	String necessary;
	List<ResponseParam> subResponseParam;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<ResponseParam> getSubResponseParam() {
		return subResponseParam;
	}

	public void setSubResponseParam(List<ResponseParam> subResponseParam) {
		this.subResponseParam = subResponseParam;
	}

	@Override
	public String toString() {
		return "ResponseParam [name=" + name + ", type=" + type + ", desc=" + desc + ", defaultValue=" + defaultValue
				+ ", subResponseParam=" + subResponseParam + "]";
	}

	public String getRealType() {
		return realType;
	}

	public void setRealType(String realType) {
		this.realType = realType;
	}

	public String getNecessary() {
		return necessary;
	}

	public void setNecessary(String necessary) {
		this.necessary = necessary;
	}
}
