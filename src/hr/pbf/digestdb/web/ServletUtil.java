package hr.pbf.digestdb.web;

import javax.servlet.http.HttpServletRequest;

public class ServletUtil {

	public static double getDouble(HttpServletRequest req, String paramName, double defaultValue) {
		String parameter = req.getParameter(paramName);
		if (parameter == null) {
			return defaultValue;
		}
		return Double.parseDouble(parameter);
	}

}
