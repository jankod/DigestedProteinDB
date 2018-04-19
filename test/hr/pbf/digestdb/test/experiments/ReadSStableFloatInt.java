package hr.pbf.digestdb.test.experiments;

import hr.pbf.digestdb.util.MyStopWatch;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.util.MemoryUtils;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

public class ReadSStableFloatInt {

    public static void main(String[] args) throws IOException {
        String path = "F:\\ProteinReader\\UniprotDBfiles\\trembl.leveldb.index.csv.sstable";

        MyStopWatch s = new MyStopWatch();
        Volume volume = MappedFileVol.FACTORY.makeVolume(path, true);
        SortedTableMap<Float, Integer> map = SortedTableMap.open(
                volume,
                Serializer.FLOAT,
                Serializer.INTEGER
        );
     

//        ConcurrentNavigableMap<Float, Integer> res = map.map(400f, 1456f);
//        System.out.println(res.size());
//        for (Map.Entry<Float, Integer> entry : res.entrySet()) {
//            System.out.println(entry);
//        }
      
        
        System.out.println("cekam");
        System.in.read();

        float mass = 1345.23f;
        Integer resInt = map.get(mass);
        System.out.println("mass " + mass + " res " + resInt);
        map.close();
        s.printDuration("t");
    }

}
