package hr.pbf.digestdb.web;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import hr.pbf.digestdb.uniprot.UniprotFormat3;
import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.util.BiteUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder;
import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder.IndexResult;
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
        IndexResult res = finder.searchIndex(massFrom, massTo);

        Set<Entry<Float, Integer>> entrySet = res.map.entrySet();
        ArrayList<Row> result = new ArrayList<>();
        int i = 0;
        for (Entry<Float, Integer> entry : entrySet) {
            Row r = new Row(i++, entry.getKey(), "pepr " + i, "prot " + i, "taxonomy " + i);
            result.add(r);
        }

    }

    private void searchByServerSideProcessingNew(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }

    private synchronized void searchByServerSideProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                massFrom = dtReq.getMassFrom().floatValue();
                massTo = dtReq.getMassTo().floatValue();
                if(massFrom >= massTo) {
                    dtResp.setError("massFrom is larger than massTo  ");
                    send(resp,dtResp );
                    return;
                }

            } catch (NullPointerException e) {
                log.error("Error " + e.getMessage());
                massFrom = 1000f;
                massTo = 1000.2f;
            }
         //   log.debug("Search mases from: {} to: {}", massFrom, massTo);
            IndexResult index = finder.searchIndex(massFrom, massTo);

            log.debug("Index foudnd {} masses with {} peptides", index.countMasses(), index.countTotalPeptides());


            long totalPeptides = index.countTotalPeptides();
            long start = dtReq.getStart();
            //long end = dtReq.getStart() + Math.max(dtReq.getLength(), 100);

            //Set<Entry<Float, Integer>> startEnd = index.getStartEndMass(start, end, totalPeptides);

            log.debug("start: {} length: {} total peptides: {}", start, dtReq.getLength(), totalPeptides);

            dtResp.setRecordsTotal((int) totalPeptides);
            dtResp.setRecordsFiltered((int) totalPeptides);

            DB db = finder.getDb();
            Pair<Float, Long> startMassPos = index.getStartMass(start);
            log.debug("Start mass {} pos: {}", startMassPos.getKey(), startMassPos.getValue());
            if (startMassPos != null) {
                DBIterator iterator = db.iterator();
                float searchMass = startMassPos.getKey();
                iterator.seek(BiteUtil.toBytes(searchMass));


                int rowAddedCounter = 1;
                long counterAll = 1;
                long counterStart = start;
                long counterEnd = counterStart + dtReq.getLength();
                log.debug("{} - {}", counterStart, counterEnd);

                if (!iterator.hasNext()) {
                    log.debug("Find nothing for: {}", searchMass);
                }
                STOP:
                while (iterator.hasNext()) {
                    Entry<byte[], byte[]> next = iterator.next();

                    float mass = BiteUtil.toFloat(next.getKey());
                    TreeMap<String, List<UniprotModel.AccTax>> v = UniprotFormat3.uncompressPeptidesJava(next.getValue());
                    Set<Entry<String, List<UniprotModel.AccTax>>> entrySetAcc = v.entrySet();

                    for (Entry<String, List<UniprotModel.AccTax>> entry : entrySetAcc) {
                        List<UniprotModel.AccTax> value = entry.getValue();
                        String peptide = entry.getKey();
                        for (UniprotModel.AccTax accTax : value) {

                            if (counterAll > counterStart && counterAll <= counterEnd) {
                                String acc = accTax.getAcc();
                                int tax = accTax.getTax();

                                Row r = new Row((int) counterAll, mass, peptide, finder.getProtName(acc), "taxonomy " + tax);
                                result.add(r);


                            }
                            if(counterAll > counterEnd) {
                                break STOP;
                            }
                            counterAll++;
                        }
                    }
                } // end while
                iterator.close();
            } else {
                log.debug("Nothing found in index");
            }

        } catch (Throwable e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

//		log.debug("izbacujem " + result.size());


        dtResp.setData(result);

        dtResp.setDraw(dtReq.getDraw());

        send(resp, dtResp);
        log.debug("============================================Finish result size: " + result.size());
    }

    private void send(HttpServletResponse resp, DataTablesResponse<Row> dtResp) throws IOException {
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-store");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(new Gson().toJson(dtResp));
    }

    private void printReqParams(HttpServletRequest req) {
        System.out.println(req.getParameterMap());

    }
}
