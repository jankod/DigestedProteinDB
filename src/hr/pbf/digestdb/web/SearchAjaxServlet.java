package hr.pbf.digestdb.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class Row {
	int num;
	float mass;
	String peptide;
	String protein;
	String taxonomy;

	// { "data": "num" },
	// { "data": "mass" },
	// { "data": "peptide" },
	// { "data": "protein" },
	// { "data": "taxonomy" }
}

// @Data
// @AllArgsConstructor
// class Result {
//
// }

public class SearchAjaxServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(SearchAjaxServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ArrayList<Row> result = new ArrayList<>();
		DataTablesRequest dtReq = new DataTablesRequest();
		try {
			log.debug("map " + req.getParameterMap());

			Enumeration<String> names = req.getParameterNames();
			while (names.hasMoreElements()) {
				String n = (String) names.nextElement();
				log.debug("n: "+ n + " = "+ req.getParameter(n));
				dtReq = new Gson().fromJson(n, DataTablesRequest.class);
			}


//			log.debug(ToStringBuilder.reflectionToString(dtReq));
			
		} catch (Throwable e) {
			log.error("", e);
			throw new RuntimeException(e);
		}

		for (int i = dtReq.getStart(); i < dtReq.getStart() + dtReq.getLength(); i++) {
			Row r = new Row(i, i + 2323.323f, "pepr " + i, "prot " + i, "taxonomy " + i);
			result.add(r);
		}
		log.debug("izbacujem " + result.size());

		DataTablesResponse<Row> dtResp = new DataTablesResponse<>();
		dtResp.setData(result);
		dtResp.setRecordsTotal(122);
		dtResp.setRecordsFiltered(122);
		dtResp.setDraw(dtReq.getDraw());

		resp.setContentType("application/json");
		resp.setHeader("Cache-Control", "no-store");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(new Gson().toJson(dtResp));
	}

	private void printReqParams(HttpServletRequest req) {
		System.out.println(req.getParameterMap());

	}
}