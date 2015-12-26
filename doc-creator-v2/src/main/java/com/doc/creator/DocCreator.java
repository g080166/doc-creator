package com.doc.creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class DocCreator {
	Set<String> fileDirSet = new HashSet<String>();
	Set<String> controllerSet = new HashSet<String>();
	Map<String, String> requestMap = new HashMap<String, String>();
	Map<String, String> responseMap = new HashMap<String, String>();
	Map<String, Map<String, String>> interfaceMap = new HashMap<String, Map<String, String>>();
	public void parseController() throws IOException {
		for (String controllerDir : controllerSet) {
			String content = getContent(controllerDir);

			getInterface(content);
		}
	}

	public void getInterface(String content) {
		String namespaceRegex = "@RequestMapping\\(\"(.*?)\"\\)";
		Pattern namespacePattern = Pattern.compile(namespaceRegex);
		Matcher namespaceMatcher = namespacePattern.matcher(content);
		String namespace = "";
		if(namespaceMatcher.find()){
			namespace = namespaceMatcher.group(1);
		}
		String regex = "/\\*([\\s\\S]*?)\\*/";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		Map<String, String> map = new HashMap<String, String>();
		int index = 0;
		String url = "";
		String rootRequestParam = "";
		String rootResponseParam = "";
		String desc = "";
		String name = "";
		while (m.find()) {
			url = getRelatedUrl(m.group(1));
			rootRequestParam = getRequest(m.group(1));
			rootResponseParam = getResponse(m.group(1));
			desc = getDesc(m.group(1));
			name = getName(m.group(1));

			if (!"".equals(url)) {
				Map<String, String> interfaceInfo = new HashMap<String, String>();
				interfaceInfo.put("interface-url", namespace + "/" + url);
				interfaceInfo.put("root-request", rootRequestParam);
				interfaceInfo.put("root-response", rootResponseParam);
				interfaceInfo.put("desc", desc);
				interfaceInfo.put("name", name);
				interfaceMap.put(namespace + "/" + url, interfaceInfo);
			}
		}

	}

	public String getRelatedUrl(String content) {
		String regex = "\\!\\!\\!url\\[(.*?)\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String url = "";
		if (m.find()) {
			url = m.group(1);
		}
		return url;

	}

	public String getRequest(String content) {
		String regex = "\\!\\!\\!request\\[(.*?)\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String req = "";
		if (m.find()) {
			req = m.group(1);
		}
		return req;

	}

	public String getResponse(String content) {
		String regex = "\\!\\!\\!response\\[(.*?)\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String resp = "";
		if (m.find()) {
			resp = m.group(1);
		}
		return resp;

	}

	public String getName(String content) {
		String regex = "\\!\\!\\!name\\[(.*?)\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String name = "";
		if (m.find()) {
			name = m.group(1);
		}
		return name;

	}

	public String getDesc(String content) {
		String regex = "\\!\\!\\!desc\\[(.*?)\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String desc = "";
		if (m.find()) {
			desc = m.group(1);
		}
		return desc;

	}

	public String getContent(String dir) throws IOException {
		FileInputStream fis =null;
		try {
			File file = new File(dir);
			String content = "";
			if (file.exists()) {
				fis = new FileInputStream(file);
				byte[] bytes = new byte[4096];
				int len = -1;
				while ((len = fis.read(bytes)) > 0) {
					content += new String(bytes, "utf-8");
				}
			} else {
				System.out.println("file not exist");
			}
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != fis){
				fis.close();
			}
		}


		return "";
	}

	public void addFileMap(List<String> projectDirList) {
		for(String projectDir:projectDirList){
			File file = new File(projectDir);
			if (!file.exists()) {
				System.out.println("�ļ���" + projectDir + "������");
				return;
			}
			getFile(projectDir);
		}
	}

	public void getFile(String filePath) {
		File file = new File(filePath);
		fileDirSet.add(file.getAbsolutePath());

		if (filePath.indexOf("Controller.java") != -1) {
			controllerSet.add(filePath);
		}

		if (!file.exists()) {
			return;
		}

		if (file.isFile()) {
			return;
		}
		String baseDir = file.getAbsolutePath();
		String[] filePathArray = file.list();
		for (String _filePath : filePathArray) {
			// System.out.println("�ļ�·����" + baseDir + File.separator +
			// _filePath);
			getFile(baseDir + File.separator + _filePath);
		}
	}

	public void parseResponse() {
		Set<String> interfaceUrls = interfaceMap.keySet();
		for (String interfaceUrl : interfaceUrls) {
			Map<String, String> interfaceInfo = interfaceMap.get(interfaceUrl);
			String rootRequest = interfaceInfo.get("root-response");
			for (String fileDir : fileDirSet) {
				if (fileDir.indexOf("\\" + rootRequest + ".java") != -1) {
					System.out.println(fileDir);
					Map<String, ResponseParam> response = parseResponse(fileDir);
					String responseJSONString = JSONObject.toJSONString(response);
					responseMap.put(interfaceUrl, responseJSONString);
				}
			}
		}
	}

	public Map<String, ResponseParam> parseResponse(String fileDir) {
		String content;
		try {
			content = getContent(fileDir);
			Map<String, Set<String>> map = getClazzInfo(content);
			Set<String> params = parseParams(content);
			Map<String, ResponseParam> responseParams = toResponseParam(params, map);
			System.out.println(JSONObject.toJSONString(responseParams));
			return responseParams;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public void parseRequest() {
		Set<String> interfaceUrls = interfaceMap.keySet();
		for (String interfaceUrl : interfaceUrls) {
			Map<String, String> interfaceInfo = interfaceMap.get(interfaceUrl);
			String rootRequest = interfaceInfo.get("root-request");
			for (String fileDir : fileDirSet) {
				if (fileDir.indexOf("\\" + rootRequest + ".java") != -1) {
					Map<String, Map<String, RequestParam>> requestParamMap = parseRequest(fileDir);
					requestMap.put(interfaceUrl, JSONObject.toJSONString(requestParamMap));
				}
			}
		}
	}

	public Map<String, Map<String, RequestParam>> parseRequest(String fileDir) {
		try {
			String content = getContent(fileDir);
			System.out.println(fileDir);
			Map<String, Set<String>> map = getClazzInfo(content);
			String extra = getExtraRequest(content);
			Map<String, RequestParam> requestExtraParamMap = new HashMap<String, RequestParam>();
			if (null != extra && !"".equals(extra)) {
				String extraDir = "";
				List<String> extraFileDirList = new ArrayList<String>();
				for (String extraFileDir : fileDirSet) {
					if (extraFileDir.indexOf(File.separator + extra + ".java") != -1) {
						extraFileDirList.add(extraFileDir);
					}
				}
				if (extraFileDirList.size() > 1) {
					Set<String> importDirSet = map.get("import");
					Set<String> packageDirSet = map.get("package");
					if (null != importDirSet && importDirSet.size() > 0) {
						for (String importDir : importDirSet) {
							if (importDir.indexOf(extra) != -1) {
								String newDir = importDir.replaceAll("\\.", File.separator);
								System.out.println(newDir);
								for (String extraFileDir : fileDirSet) {
									if (extraFileDir.indexOf(newDir + ".java") != -1) {
										extraDir = extraFileDir;
										break;
									}
								}
							}
						}
					} else {
						for (String packageDir : packageDirSet) {
							String newDir = packageDir.replaceAll("\\.", File.separator);
							newDir += File.separator + extra;
							System.out.println(newDir);
							for (String extraFileDir : fileDirSet) {
								if (extraFileDir.indexOf(newDir + ".java") != -1) {
									extraDir = extraFileDir;
									break;
								}
							}

						}
					}

				} else if (extraFileDirList.size() == 1) {
					extraDir = extraFileDirList.get(0);

				}

				if (!"".equals(extraDir)) {
					String extraContent = getContent(extraDir);
					Set<String> extraParams = parseParams(extraContent);
					requestExtraParamMap = toRequestParam(extraParams);
				}
			}

			Set<String> params = parseParams(content);
			Map<String, RequestParam> requestParamMap = toRequestParam(params);
			Map<String, Map<String, RequestParam>> allRequestParamMap = new HashMap<String, Map<String, RequestParam>>();
			if (null != requestExtraParamMap && requestExtraParamMap.size() > 0) {
				allRequestParamMap.put("extra", requestExtraParamMap);
			}

			allRequestParamMap.put("original", requestParamMap);
			System.out.println(JSONObject.toJSONString(allRequestParamMap));
			return allRequestParamMap;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	static Set<String> forbiddenParams = new HashSet<String>();

	static {
		forbiddenParams.add("public");
		forbiddenParams.add("private");
		forbiddenParams.add("default");
		forbiddenParams.add("static");
		forbiddenParams.add("final");
		forbiddenParams.add("volatile");
		forbiddenParams.add("transient");
		forbiddenParams.add("native");
		forbiddenParams.add("=");
	}

	static Set<String> baseType = new HashSet<String>();

	static {
		baseType.add("int");
		baseType.add("Integer");
		baseType.add("long");
		baseType.add("Long");
		baseType.add("short");
		baseType.add("Short");
		baseType.add("byte");
		baseType.add("Byte");
		baseType.add("double");
		baseType.add("Double");
		baseType.add("float");
		baseType.add("Float");
		baseType.add("boolean");
		baseType.add("Boolean");
		baseType.add("String");
		baseType.add("JSONArray");
		baseType.add("JSONObject");
	}

	static {
		baseType.add("int");
		baseType.add("Integer");
		baseType.add("long");
		baseType.add("Long");
		baseType.add("short");
		baseType.add("Short");
		baseType.add("byte");
		baseType.add("Byte");
		baseType.add("double");
		baseType.add("Double");
		baseType.add("float");
		baseType.add("Float");
		baseType.add("boolean");
		baseType.add("Boolean");
		baseType.add("String");
		baseType.add("JSONArray");
		baseType.add("JSONObject");
	}

	static Set<String> collectionBaseType = new HashSet<String>();

	static {
		collectionBaseType.add("List");
		collectionBaseType.add("Set");
		collectionBaseType.add("Map");
	}

	static final String TYPE_JSONARRAY = "jsonArray";
	static final String TYPE_JSONOBJECT = "jsonObject";

	public Map<String, ResponseParam> toResponseParam(Set<String> paramSet, Map<String, Set<String>> map)
			throws IOException {
		Map<String, ResponseParam> responseParamMap = new HashMap<String, ResponseParam>();
		for (String _param : paramSet) {
			String param = "";
			String desc = "";
			if (_param.indexOf("//") != -1) {
				String[] _paramArray = _param.split("\\/\\/");
				param = _paramArray[0];
				desc = _paramArray[1];
			} else {
				param = _param;
			}

			String[] paramArray = param.trim().split(" ");
			ResponseParam responseParam = new ResponseParam();
			boolean hasDefaultValue = false;
			for (String singleParam : paramArray) {
				singleParam = singleParam.trim();
				if (singleParam.equals("=")) {
					hasDefaultValue = true;
					continue;
				}

				if (forbiddenParams.contains(singleParam)) {
					continue;
				}

				if (null == responseParam.getType() || "".equals(responseParam.getType())) {
					String clazzName = "";
					if(baseType.contains(singleParam.trim())){
						responseParam.setType(singleParam);
						responseParam.setRealType(singleParam);
					} else if (isCollection(singleParam)) {
						responseParam.setType(TYPE_JSONARRAY);
						String clazzRegex = "<(.*?)>";

						Pattern p = Pattern.compile(clazzRegex);
						Matcher m = p.matcher(singleParam);
						if (m.find()) {
							clazzName = m.group(1);
						}
						responseParam.setRealType(clazzName);
					} else {
						responseParam.setType(TYPE_JSONOBJECT);
						clazzName = singleParam;
						responseParam.setRealType(clazzName);
					}

					if (null != clazzName && !clazzName.equals("")) {
						if (baseType.contains(clazzName)) {
							continue;
						}
						String fileDir = getClazzDir(clazzName, map);
						String content = getContent(fileDir);
						Map<String, Set<String>> subMap = getClazzInfo(content);
						Set<String> params = parseParams(content);
						Map<String, ResponseParam> subResponseMap = toResponseParam(params, subMap);

						if (null != subResponseMap && !subResponseMap.isEmpty()) {
							Set<String> keys = subResponseMap.keySet();
							List<ResponseParam> responseParamList = new ArrayList<ResponseParam>();
							for (String key : keys) {
								ResponseParam subResponseParam = subResponseMap.get(key);
								responseParamList.add(subResponseParam);
							}
							responseParam.setSubResponseParam(responseParamList);
						}

					}

					continue;
				}

				if (null == responseParam.getName() || "".equals(responseParam.getType())) {
					if (singleParam.indexOf(";") != -1) {
						singleParam = singleParam.substring(0, singleParam.length() - 1);
					}
					responseParam.setName(singleParam);
					continue;
				}
				if (hasDefaultValue
						&& (null == responseParam.getDefaultValue() || "".equals(responseParam.getDefaultValue()))) {
					responseParam.setDefaultValue(singleParam);
					continue;
				}

			}

			if (null != desc && !"".equals(desc)) {
				String[] descArray = desc.split(",");
				if (null != descArray && desc.length() > 0) {
					for (int index = 0; index < descArray.length; index++) {
						if (index == 0) {
							responseParam.setDesc(descArray[0]);
						} else if (index == 1) {
							responseParam.setNecessary(descArray[1]);
						}

					}
				}
			}
			System.out.println(responseParam.getName());
			responseParamMap.put(responseParam.getName(), responseParam);
		}


		return responseParamMap;
	}


	private String getClazzDir(String clazzName, Map<String, Set<String>> map) {
		Set<String> importDirList = map.get("import");
		Set<String> packageDirList = map.get("package");
		String dir = "";
		if (null != importDirList && importDirList.size() > 0) {
			for (String importDir : importDirList) {
				if (importDir.indexOf(clazzName) != -1) {
					dir = importDir.replaceAll("\\.", "\\" + File.separator);
					return getRightFileDir(dir);
				}
			}
		}

		if (null != packageDirList && packageDirList.size() > 0 && (null == dir || "".equals(dir))) {
			String packageDir = "";
			for (String _packageDir : packageDirList) {
				packageDir = _packageDir;
				break;
			}

			dir = packageDir.replaceAll("\\.", "\\" + File.separator) + File.separator + clazzName;
			return getRightFileDir(dir);
		}

		return "";
	}

	private String getRightFileDir(String dir) {
		for (String fileDir : fileDirSet) {
			if (fileDir.indexOf(dir + ".java") != -1) {
				System.out.println("�ļ���" + fileDir + ",ʵ�ʣ�" + dir);
				return fileDir;
			}
		}

		return "";
	}


	private boolean isCollection(String type) {
		boolean isCollection = false;
		for (String collectionType : collectionBaseType) {
			if (type.indexOf(collectionType) != -1) {
				isCollection = true;
			}
		}

		return isCollection;
	}

	public Map<String, RequestParam> toRequestParam(Set<String> paramSet) {
		Map<String, RequestParam> requestParamMap = new HashMap<String, RequestParam>();
		for (String _param : paramSet) {
			String param = "";
			String desc = "";
			if (_param.indexOf("//") != -1) {
				String[] _paramArray = _param.split("\\/\\/");
				param = _paramArray[0];
				desc = _paramArray[1];
			} else {
				param = _param;
			}

			String[] paramArray = param.trim().split(" ");
			RequestParam requestParam = new RequestParam();
			boolean hasDefaultValue = false;
			for (String singleParam : paramArray) {
				singleParam = singleParam.trim();
				if (singleParam.equals("=")) {
					hasDefaultValue = true;
					continue;
				}

				if (forbiddenParams.contains(singleParam)) {
					continue;
				}

				if (null == requestParam.getType() || "".equals(requestParam.getType())) {
					requestParam.setType(singleParam);
					continue;
				}

				if (null == requestParam.getName() || "".equals(requestParam.getType())) {
					if (singleParam.indexOf(";") != -1) {
						singleParam = singleParam.substring(0, singleParam.length() - 1);
					}
					requestParam.setName(singleParam);
					continue;
				}
				if (hasDefaultValue
						&& (null == requestParam.getDefaultValue() || "".equals(requestParam.getDefaultValue()))) {
					requestParam.setDefaultValue(singleParam);
					continue;
				}

			}

			if (null != desc && !"".equals(desc)) {
				String[] descArray = desc.split(";");
				if (null != descArray && desc.length() > 0) {
					for (int index = 0; index < descArray.length; index++) {
						if (index == 0) {
							requestParam.setDesc(descArray[0]);
						} else if (index == 1) {
							requestParam.setNecessary(descArray[1]);
						}

					}
				}
			}

			requestParamMap.put(requestParam.getName(), requestParam);
		}

		return requestParamMap;
	}

	public Set<String> parseParams(String content) {
		String[] clazzBody = content.split("\\n");

		Boolean isClassBody = false;
		Boolean isMethodBody = false;
		Set<String> params = new HashSet<String>();
		for (int index = 0; index < clazzBody.length; index++) {
			String line = clazzBody[index];
			if (line.indexOf("package") != -1)
				continue;
			if (line.indexOf("import") != -1)
				continue;

			if (!isClassBody && line.indexOf("{") != -1) {
				isClassBody = true;
				continue;
			}
			
			if (isClassBody) {

				String regex = "\\(.*?\\)|\\(|\\)";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(line);
				if (m.find()) {
					isMethodBody = true;
					continue;
				}


				if (line.indexOf("{") != -1) {
					isMethodBody = true;
					continue;
				}
				if (line.trim().startsWith("//")) {
					continue;
				}

				if (isMethodBody && line.indexOf("}") == -1) {
					continue;
				}
				if (isMethodBody && line.indexOf("}") != -1) {
					isMethodBody = false;
					continue;
				}
				if (!isMethodBody && line.indexOf("}") != -1) {
					break;
				}

				if (!isMethodBody && !line.trim().equals("") && line.indexOf("@") == -1) {
					params.add(line);
					continue;
				}


			}
		}

		return params;
	}

	public Map<String, Set<String>> getClazzInfo(String content) {
		String[] lines = content.split("\\n");
		Set<String> packageSet = new HashSet<String>();
		Set<String> importSet = new HashSet<String>();
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("package")) {
				packageSet.add(line.substring(7, line.length() - 1).trim());
				map.put("package", packageSet);
			}

			if (line.startsWith("import")) {
				importSet.add(line.substring(6, line.length() - 1).trim());
				map.put("import", importSet);
			}
		}
		return map;
	}

	public String getExtraRequest(String content) {
		if (null == content || "".equals(content)) {
			return null;
		}

		if (content.indexOf("extends") == -1) {
			return null;
		}

		String regex = "extends\\s+(\\w+).*?\\s";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		String extra = "";
		if (m.find()) {
			extra = m.group(1);
		}
		return extra;
	}

	public JSONArray parseJSON() {
		JSONArray jsonArray = new JSONArray();
		Set<String> keySet = interfaceMap.keySet();
		for (String interfaceUrl : keySet) {
			System.out.println("解析地址：" + interfaceUrl);
			Map<String, String> interfaceInfoMap = interfaceMap.get(interfaceUrl);

			if (null == interfaceInfoMap || interfaceInfoMap.isEmpty()) {
				continue;
			}
			String name = interfaceInfoMap.get("name");
			String desc = interfaceInfoMap.get("desc");
			// String necessary = interfaceInfoMap.get("necessary");
			String request = requestMap.get(interfaceUrl);
			System.out.println("request:" + request);

			String response = responseMap.get(interfaceUrl);
			System.out.println("response:" + response);

			JSONObject jsonObjct = new JSONObject();

			jsonObjct.put("name", name);
			jsonObjct.put("desc", desc);
			// jsonObjct.put("necessary", necessary);
			jsonObjct.put("interfaceUrl", interfaceUrl);
			JSONObject requestJSONObject = JSONObject.parseObject(request);
			JSONObject originalRequestJSONObject = requestJSONObject.getJSONObject("original");
			Set<String> originalRequestKeys = originalRequestJSONObject.keySet();
			JSONArray originalRequestJSONArray = new JSONArray();
			for (String originalRequestKey : originalRequestKeys) {
				originalRequestJSONArray.add(originalRequestJSONObject.get(originalRequestKey));
			}
			jsonObjct.put("originalRequest", originalRequestJSONArray);

			if (requestJSONObject.containsKey("extra")) {
				JSONObject extraRequestJSONObject = requestJSONObject.getJSONObject("extra");
				Set<String> extraRequestKeys = extraRequestJSONObject.keySet();
				JSONArray extraRequestJSONArray = new JSONArray();
				for (String extraRequestKey : extraRequestKeys) {
					extraRequestJSONArray.add(extraRequestJSONObject.get(extraRequestKey));
				}
				jsonObjct.put("extraRequest", extraRequestJSONArray);
			}

			JSONObject responseJSONObject = JSONObject.parseObject(response);

			JSONArray responseJSONArray = new JSONArray();
			Set<String> responseKeys = responseJSONObject.keySet();
			for (String responseKey : responseKeys) {
				JSONObject singleResponseJSONObj = responseJSONObject.getJSONObject(responseKey);
				responseJSONArray.add(singleResponseJSONObj);
			}
			jsonObjct.put("response", responseJSONArray);
			jsonArray.add(jsonObjct);
		}
		return jsonArray;
	}

	static final String jsonFileDir = "E:\\1-WorkSpace\\3-DevelopWorkSpace\\HuaerWeiGuanjiaWorkSpace\\project\\tmp";

	public void write2File(String content) throws IOException {
		String jsonFileName = "interface.json";
		String fileDir = jsonFileDir + File.separator + jsonFileName;
		System.out.println(fileDir);

		File file = new File(fileDir);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(file);
		// FileOutputStream fos = new FileOutputStream(file);
		System.out.println(content);
		// fos.write(content.getBytes("utf-8"));
		fw.write(content);
		if (null != fw) {
			fw.close();
		}
		System.out.println("生成的文件路径" + fileDir + "成功");
	}

	public void create(List<String> projectDirList) throws IOException {
		addFileMap(projectDirList);
		parseController();
		parseRequest();
		parseResponse();

		JSONArray jsonArray = parseJSON();
		write2File(jsonArray.toJSONString());

	}

	public static void main(String[] args) throws IOException {
		String projectDir = "E:\\1-WorkSpace\\3-DevelopWorkSpace\\HuaerWeiGuanjiaWorkSpace\\project\\media-web";
		String projectDir2 = "E:\\1-WorkSpace\\3-DevelopWorkSpace\\HuaerWeiGuanjiaWorkSpace\\project\\media-common";
		List<String> projectDirList = new ArrayList<String>();
		projectDirList.add(projectDir);
		projectDirList.add(projectDir2);
		DocCreator docCreator = new DocCreator();
		docCreator.create(projectDirList);
	}
}
