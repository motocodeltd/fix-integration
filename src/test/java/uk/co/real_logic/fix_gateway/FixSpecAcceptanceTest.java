package uk.co.real_logic.fix_gateway;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.agrona.LangUtil;
import uk.co.real_logic.fix_gateway.environments.Environment;
import uk.co.real_logic.fix_gateway.environments.QuickFixToGatewayEnvironment;
import uk.co.real_logic.fix_gateway.steps.TestStep;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static uk.co.real_logic.fix_gateway.system_tests.SystemTestUtil.launchMediaDriver;

@RunWith(Parameterized.class)
public class FixSpecAcceptanceTest
{
    private static final String ROOT_PATH = "src/test/resources/quickfixj_definitions/fix44";

    // TODO: Failing
    // "7_ReceiveRejectMessage.def"

    /** banned acceptance tests - not part of the spec we're aiming to support */
    private static final List<String> BANNED = Arrays.asList(
        "2i_BeginStringValueUnexpected.def"
    );

    private static final List<String> CURRENTLY_PASSING = Arrays.asList(

        // "4b_ReceivedTestRequest.def"
        // "13b_UnsolicitedLogoutMessage.def"
        // "QFJ648_NegativeHeartBtInt.def"
        // "QFJ650_MissingMsgSeqNum.def"
        // "2c_MsgSeqNumTooLow.def"
        // "1a_ValidLogonWithCorrectMsgSeqNum.def"
        // "1c_InvalidTargetCompID.def"
        // "1c_InvalidSenderCompID.def",
        // "1d_InvalidLogonLengthInvalid.def"
        // "1d_InvalidLogonBadSendingTime.def"
        // "1d_InvalidLogonLengthInvalid.def"
        // "1d_InvalidLogonWrongBeginString.def"
        // "1e_NotLogonMessage.def"
    );

    private List<TestStep> steps;
    private MediaDriver mediaDriver;

    @Parameterized.Parameters(name = "Acceptance: {1}")
    public static Collection<Object[]> data()
    {
        try
        {
            return currentPassingTests()
                .map(path -> new Object[]{path, path.getFileName()})
                .collect(toList());
        }
        catch (Exception e)
        {
            LangUtil.rethrowUnchecked(e);
            return null;
        }
    }

    private static Stream<Path> currentPassingTests()
    {
        return CURRENTLY_PASSING.stream().map(file -> Paths.get(ROOT_PATH, file));
    }

    // TODO: enable all tests when ready
    private static Stream<Path> allTests() throws IOException
    {
        return Files.list(Paths.get(ROOT_PATH)).filter(path -> !BANNED.contains(path.getFileName().toString()));
    }

    public FixSpecAcceptanceTest(final Path path, final Path filename)
    {
        steps = TestStep.load(path);
        mediaDriver = launchMediaDriver();
    }

    @Test
    public void shouldPassAcceptanceCriteria() throws Exception
    {
        try (final Environment environment = new QuickFixToGatewayEnvironment())
        {
            steps.forEach(step -> step.perform(environment));
        }
    }

    @After
    public void shutdown()
    {
        if (mediaDriver != null)
        {
            mediaDriver.close();
        }
    }

}