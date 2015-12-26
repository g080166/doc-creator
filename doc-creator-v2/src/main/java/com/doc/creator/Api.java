package com.doc.creator;

import java.util.List;

public class Api {
	String url;
	List<RequestParam> requestParams;
	List<RequestParam> extraRequestParams;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<RequestParam> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(List<RequestParam> requestParams) {
		this.requestParams = requestParams;
	}
}
