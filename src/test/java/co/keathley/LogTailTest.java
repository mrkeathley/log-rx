package co.keathley;

import org.junit.Assert;
import org.junit.Test;
import rx.Subscription;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;

/**
 * Tests of the LogTail class
 * Created by mkeathley on 4/13/16.
 */
public class LogTailTest {

    @Test
    public void testSimpleRead() {
        try {
            File file = new File("src/test/resources/logs/apache.sample.log");

            // Create LogTail and send events into a StringBuilder
            LogTail tail = new LogTail(file);
            StringBuilder output = new StringBuilder();
            final Subscription subscription = tail.observe().subscribe((LogTail.LogTailEvent next) -> {
                output.append(new String(next.getOutputStream().toByteArray()));
            });

            // Read the file
            StringBuilder input = new StringBuilder();
            FileReader fileReader = new FileReader(file);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            while (fileReader.ready()) {
                os.write(fileReader.read());
            }
            input.append(new String(os.toByteArray()));

            // Buffere in case LogTail is not complete
            if (output.length() == 0) Thread.sleep(5000);

            subscription.unsubscribe();

            // Assert read was correct
            Assert.assertEquals(input.toString(), output.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
