package hr.pbf.digestdb.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder;
import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder.SearchIndexResult;
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
}


public class SearchAjaxServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(SearchAjaxServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        searchByServerSideProcessing(req, resp);
        // searchByAjax(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //	searchByAjax(req, resp);
    }

    private void searchByAjax(HttpServletRequest req, HttpServletResponse resp) {
        UniprotLevelDbFinder finder = WebListener.getFinder();
        float massFrom;
        float massTo;
        try {
            massFrom = Float.parseFloat(req.getParameter("massFrom"));
            massTo = Float.parseFloat(req.getParameter("massTo"));
        } catch (NullPointerException e) {
            massFrom = 400;
            massTo = 6000;
        }
        SearchIndexResult res = finder.searchIndex(massFrom, massTo);

        Set<Entry<Float, Integer>> entrySet = res.subMap.entrySet();
        ArrayList<Row> result = new ArrayList<>();
        int i = 0;
        for (Entry<Float, Integer> entry : entrySet) {
            Row r = new Row(i++, entry.getKey(), "pepr " + i, "prot " + i, "taxonomy " + i);
            result.add(r);
        }

    }

    private void searchByServerSideProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ArrayList<Row> result = new ArrayList<>();
        DataTablesRequest dtReq = new DataTablesRequest();
        DataTablesResponse<Row> dtResp = new DataTablesResponse<>();
        try {
            // log.debug("map " + req.getParameterMap());

            Enumeration<String> names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String n = (String) names.nextElement();
//				log.debug("n: " + n + " = " + req.getParameter(n));
                dtReq = new Gson().fromJson(n, DataTablesRequest.class);
            }
            UniprotLevelDbFinder finder = WebListener.getFinder();
            float massFrom;
            float massTo;
            try {
                //massFrom = Float.parseFloat(req.getParameter("massFrom"));
                //massTo = Float.parseFloat(req.getParameter("massTo"));
                massFrom = dtReq.getMassFrom().floatValue();
                massTo = dtReq.getMassTo().floatValue();

            } catch (NullPointerException e) {
                log.error("Error ", e);
                massFrom = 500;
                massTo = 6000;
            }
            log.debug("Search {} {}", massFrom, massTo);
            SearchIndexResult res = finder.searchIndex(massFrom, massTo);
            // log.debug(ToStringBuilder.reflectionToString(dtReq));

            Set<Entry<Float, Integer>> entrySet = res.subMap.entrySet();
            log.debug("Index foudnd masses"+ entrySet.size());



            long totalPeptides = res.countTotalPeptides();
            long start = dtReq.getStart();
            long end = dtReq.getStart() + dtReq.getLength();
            log.debug("start {} end {} total {}", start, end, totalPeptides);

            dtResp.setRecordsTotal((int) totalPeptides);
            dtResp.setRecordsFiltered((int) totalPeptides);

            for (long i = start; i < end; i++) {
                Row r = new Row((int) i, (float) i, "pepr " + i, "prot " + i, "taxonomy " + i);
                result.add(r);
            }
            for (long i = dtReq.getStart(); i < dtReq.getStart() + dtReq.getLength(); i++) {
                // Row r = new Row(i, i + 2323.323f, "pepr " + i, "prot " + i, "taxonomy " + i);
                // result.add(r);
            }

        } catch (Throwable e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

//		log.debug("izbacujem " + result.size());


        dtResp.setData(result);

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