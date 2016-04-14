package co.keathley;

import rx.Observable;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Performs Generic LogFile Tailing with a provided fidelity.
 *
 * @author Matthew Keathley
 */
public class LogTail {

    private final String logName;
    private final FileReader fileReader;
    private final long fidelity;
    private final long skip;

    /**
     * Creates a LogTail with the provided file. The fidelity is set to 5ms and the reader is skipped to the end of the current file.
     *
     * @param file The file to tail.
     * @throws FileNotFoundException
     */
    public LogTail(File file) throws FileNotFoundException {
        this(file, 5, -1);
    }

    /**
     * Creates a LogTaile with the provided file, read fidelity, and selected skip.
     *
     * @param file     The file to tail.
     * @param fidelity The read fidelity in milliseconds.
     * @param skip     How far to skip in the initial file
     * @throws FileNotFoundException
     */
    public LogTail(File file, long fidelity, long skip) throws FileNotFoundException {
        fileReader = new FileReader(file);
        logName = file.getName();
        this.fidelity = fidelity;

        if (skip == -1) {
            this.skip = file.length();
        } else {
            this.skip = skip;
        }
    }

    /**
     * Return the Observable of LogTailEvents.
     *
     * @return
     */
    public Observable<LogTailEvent> observe() {
        return Observable.create(asyncOnSubscribe -> {
            final Timer timer = new Timer();

            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    try {
                        if (asyncOnSubscribe.isUnsubscribed()) {
                            timer.cancel();
                            cancel();
                        }

                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        while (fileReader.ready()) {
                            os.write(fileReader.read());
                        }

                        if (os.size() > 0)
                            asyncOnSubscribe.onNext(new LogTailEvent(logName, os));
                    } catch (IOException ex) {

                    }
                }
            };

            timer.schedule(task, 0, fidelity);
        });
    }

    /**
     * An event sent after each section of
     */
    public static final class LogTailEvent {
        private final String logName;
        private final ByteArrayOutputStream outputStream;

        public LogTailEvent(String logName, ByteArrayOutputStream outputStream) {
            this.logName = logName;
            this.outputStream = outputStream;
        }

        public ByteArrayOutputStream getOutputStream() {
            return outputStream;
        }

        public String getLogName() {
            return logName;
        }
    }

}