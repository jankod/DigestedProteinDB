package hr.pbf.digestdb.workflow.core;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class JobContext {

	public JobContext() {

	}

	private final Map<String, Object> params = new HashMap<>();

	public void setParam(String key, Object value) {
		params.put(key, value);
	}

	public Object getParam(String key) {
		return params.get(key);
	}


}
