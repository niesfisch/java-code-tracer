package de.marcelsauer.profiler.processor.file;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.processor.AbstractAsyncStackProcessor;
import de.marcelsauer.profiler.processor.RecordingEvent;

/**
 * writes data to configured file
 */
public class AsyncFileWritingStackProcessor extends AbstractAsyncStackProcessor {

    private static final Logger logger = Logger.getLogger(AsyncFileWritingStackProcessor.class);
    private static final String STACK_DIR = Config.get().processor.stackFolderName;

    @Override
    protected void doStart() {
        createStackFolderIfNotPresent();
    }

    @Override
    protected void doStop() {
        // nothing to do here
    }

    @Override
    protected void doProcess(Collection<RecordingEvent> snapshots) {
        String outFilename = getOutFilename();
        try {
            for (RecordingEvent event : snapshots) {
                writeEventToFile(event, outFilename);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEventToFile(RecordingEvent event, String outFilename) throws IOException {
        String json = event.asJson();
        Files.write(Paths.get(outFilename), (json + "\n").getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private String getOutFilename() {
        String fileDateTimePart = new SimpleDateFormat("yyyy_dd_MM").format(new Date());
        return String.format("%s/jct_%s.log", STACK_DIR, fileDateTimePart);
    }

    private void createStackFolderIfNotPresent() {
        try {
            Files.createDirectory(Paths.get(STACK_DIR));
        } catch (FileAlreadyExistsException e) {
            // ignore
        } catch (IOException e) {
            throw new RuntimeException("problem with output directory " + STACK_DIR, e);
        }
    }
}
