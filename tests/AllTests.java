package com.amalaei.engineering;

import org.junit.Test;

public final class AllTests {
    @Test public void analysis() throws Exception { AnalysisTest.main(new String[0]); }
    @Test public void backupPartition() throws Exception { BackupPartitionTest.main(new String[0]); }
    @Test public void backupCipher() throws Exception { BackupCipherTest.main(new String[0]); }
    @Test public void callCsvParser() { CallCsvParserTest.main(new String[0]); }
    @Test public void calculatorScenarios() { CalculatorScenarioTest.main(new String[0]); }
    @Test public void followUps() { FollowUpsTest.main(new String[0]); }
    @Test public void importAnalysis() { ImportAnalysisTest.main(new String[0]); }
    @Test public void profiles() { ProfilesTest.main(new String[0]); }
    @Test public void quoteMath() { QuoteMathTest.main(new String[0]); }
    @Test public void softwareWork() throws Exception { SoftwareWorkTest.main(new String[0]); }
    @Test public void surveyRequests() { SurveyRequestsTest.main(new String[0]); }
}
