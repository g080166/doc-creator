package com.doc.creator;

import java.util.List;

public class RequestParam {
	String name;
	String defaultValue;
	String type;
	String desc;
	String necessary;
	List<RequestParam> subRequestParam;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
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

	public List<RequestParam> getSubRequestParam() {
		return subRequestParam;
	}

	public void setSubRequestParam(List<RequestParam> subRequestParam) {
		this.subRequestParam = subRequestParam;
	}

	public String getNecessary() {
		return necessary;
	}

	public void setNecessary(String necessary) {
		this.necessary = necessary;
	}
}
