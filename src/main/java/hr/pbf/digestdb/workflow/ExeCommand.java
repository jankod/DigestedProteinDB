package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.workflow.core.Job;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;


/**
 * export TMPDIR=/disk4/janko/temp_dir # Stvorite ovaj direktorij ako ne postoji
 * sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
 */
@Data
@Slf4j
public class ExeCommand implements Job<Integer> {

    private String cmd;
    private File dir;

    @Override
    public Integer start() throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("bash", "-c", cmd);
        if (dir != null) {
            builder.directory(dir);
        }
        try {
            Process process = builder.start();
            return process.waitFor();
        } catch (Exception e) {
            log.error("Error executing command: {}", cmd, e);
            throw new RuntimeException("Error executing command: " + cmd, e);
        }
    }


}
