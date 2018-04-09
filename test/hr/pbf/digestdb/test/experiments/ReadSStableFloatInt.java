package hr.pbf.digestdb.test.experiments;

import hr.pbf.digestdb.util.MyStopWatch;
import org.apache.commons.lang3.time.StopWatch;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

public class ReadSStableFloatInt {

    public static void main(String[] args) {
        String path = "F:\\ProteinReader\\UniprotDBfiles\\trembl.leveldb.index.csv.sstable";


        Volume volume = MappedFileVol.FACTORY.makeVolume(path, true);
        SortedTableMap<Float, Integer> map = SortedTableMap.open(
                volume,
                Serializer.FLOAT,
                Serializer.INTEGER
        );

//        ConcurrentNavigableMap<Float, Integer> res = map.subMap(400f, 1456f);
//        System.out.println(res.size());
//        for (Map.Entry<Float, Integer> entry : res.entrySet()) {
//            System.out.println(entry);
//        }
        MyStopWatch s = new MyStopWatch();

        float mass = 1345.23f;
        Integer resInt = map.get(mass);
        System.out.println("mass " + mass + " res " + resInt);
        map.close();
        s.printDuration("t");
    }

}
