package sap.prd.cmintegration.cli;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.sap.cmclient.dto.Transport;

public class ABAPBackendIsTransportModifiableTest extends ABAPBackendTransportTest {

    private static Transport releasedTransport = getReleasedTransport(),
                             transportInDevelopment = getTransportInDevelopment();

    private static Transport getReleasedTransport() {
        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Status", "R"); // Released
        return new Transport(m);
    }

    private static Transport getTransportInDevelopment() {
        Map<String, Object> m = Maps.newHashMap();
        m.put("Id", "999");
        m.put("Status", "D"); // In development
        return new Transport(m);
    }

    @Test
    public void testGetTransportIsModifiablesForTransportInDevelopmentReturnsTrue() throws Exception {

        setMock(setupGetTransportMock(transportInDevelopment));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "is-transport-modifiable",
                        "-tID", "999"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("true")));
    }


    @Test
    public void testGetTransportIsModifiableForReleasedTransportReturnsFalse() throws Exception {

        setMock(setupGetTransportMock(releasedTransport));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "is-transport-modifiable",
                        "-tID", "999"});

        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("false")));
    }

    @Test
    public void testGetTransportIsModifiablesForTransportInDevelopmentReturnsZeroReturnCode() throws Exception {

        // zero return code is expressed by the absence of an exception, especially the absense of
        // ExitException bearing return code not equals to 0.

        setMock(setupGetTransportMock(transportInDevelopment));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "is-transport-modifiable",
                        "--return-code",
                        "-tID", "999"});

        // nothing must be emitted to STDOUT in this case.
        assertThat(removeCRLF(IOUtils.toString(result.toByteArray(), "UTF-8")), is(equalTo("")));
    }

    @Test
    public void testGetTransportIsModifiableForReleasedTransportReturnsNonZeroReturnCode() throws Exception {

        thrown.expect(ExitException.class);
        thrown.expect(new BaseMatcher<Exception>() {

            private int exitCode = -1;

            @Override
            public boolean matches(Object item) {
                if(! (item instanceof ExitException)) {
                    return false;
                }
                exitCode = ((ExitException)item).getExitCode();
                return exitCode == ExitException.ExitCodes.FALSE;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(format("Unexpected exit code received: %d", exitCode));
            }
        });

        setMock(setupGetTransportMock(releasedTransport));

        Commands.main(new String[]
                {       "-e", "http://example.org:8000/endpoint",
                        "-u", "me",
                        "-p", "openSesame",
                        "-t", "ABAP",
                        "is-transport-modifiable",
                        "--return-code",
                        "-tID", "999"});
    }
}

