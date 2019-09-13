package com.chikli.hudson.plugin.naginator.pipeline;

import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;

/**
 * Tests {@link RetryExtStep}.
 */
public class RetryExtStepTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Test the regexp configuration for a WorkflowJob project.
     * <code>regexpForMatrixParent</code> is not preserved as not displayed.
     * 
     * @throws Exception
     */
    @Test
    public void testConfigurationForRegexpOnWorkflowJob() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "p");

        job.setDefinition(new CpsFlowDefinition(
            "int i = 0;\n" +
            "retryExt( retry: 3, ) {\n" +
            "         regexpForRerun: 'Some Regular Expression',\n" +
            "         rerunIfUnstable: false,\n" +
            "         checkRegexp: true,\n" +
            "         maxSchedule: 1,\n" +
            "    println 'Trying!'\n" +
            "    if (i++ < 2) error('oops');\n" +
            "    println 'Done!'\n" +
            "}\n" +
            "println 'Over!'"
        , true));

        WorkflowRun run = j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        j.assertLogNotContains("retryExt", run);
    }


    @Test
    public void smokes() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
            "int i = 0;\n" +
            "retryExt( retry: 3 ) {\n" +
            "    println 'Trying!'\n" +
            "    if (i++ < 2) error('oops');\n" +
            "    println 'Done!'\n" +
            "}\n" +
            "println 'Over!'"
        , true));

        QueueTaskFuture<WorkflowRun> f = p.scheduleBuild2(0);
        WorkflowRun b = j.assertBuildStatusSuccess(f);

        String log = JenkinsRule.getLog(b);
        j.assertLogNotContains("\tat ", b);

        int idx = 0;
        for (String msg : new String[] {
            "Trying!",
            "oops",
            "Retrying",
            "Trying!",
            "oops",
            "Retrying",
            "Trying!",
            "Done!",
            "Over!",
        }) {
            idx = log.indexOf(msg, idx + 1);
            assertTrue(msg + " not found", idx != -1);
        }

        idx = 0;
        for (String msg : new String[] {
            "[Pipeline] retryExt",
            "[Pipeline] {",
            "[Pipeline] }",
            "[Pipeline] {",
            "[Pipeline] }",
            "[Pipeline] {",
            "[Pipeline] }",
            "[Pipeline] // retryExt",
        }) {
            idx = log.indexOf(msg, idx + 1);
            assertTrue(msg + " not found", idx != -1);
        }
    }

}
