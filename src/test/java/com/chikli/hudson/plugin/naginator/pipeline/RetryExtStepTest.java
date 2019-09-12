package com.chikli.hudson.plugin.naginator.pipeline;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Result;
import hudson.model.User;
import hudson.model.queue.QueueTaskFuture;
import hudson.security.ACL;
import hudson.security.ACLContext;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;

/**
 * Tests {@link RetryStep}.
 */
public class RetryExtStepTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public JenkinsRule j = new JenkinsRule();

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

    @Test
    public void abortShouldNotRetry() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "int count = 0; retryExt(retry: 3) { echo 'trying '+(count++); semaphore 'start'; echo 'NotHere' } echo 'NotHere'", true));
        final WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        SemaphoreStep.waitForStart("start/1", b);
        try (ACLContext context = ACL.as(User.getById("dev", true))) {
            b.getExecutor().doStop();
        }
        j.assertBuildStatus(Result.ABORTED, j.waitForCompletion(b));
        j.assertLogContains("trying 0", b);
        j.assertLogContains("Aborted by dev", b);
        j.assertLogNotContains("trying 1", b);
        j.assertLogNotContains("trying 2", b);
        j.assertLogNotContains("NotHere", b);

    }

    @Test
    public void inputAbortShouldNotRetry() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("int count = 0\n" +
                "retryExt(retry: 3) {\n" +
                "  echo 'trying '+(count++)\n" +
                "  input id: 'InputX', message: 'OK?', ok: 'Yes'\n" +
                "}\n", true));

        QueueTaskFuture<WorkflowRun> queueTaskFuture = p.scheduleBuild2(0);
        WorkflowRun run = queueTaskFuture.getStartCondition().get();
        CpsFlowExecution execution = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            execution.waitForSuspension();
        }

        InputAction inputAction = run.getAction(InputAction.class);
        InputStepExecution is = inputAction.getExecution("InputX");
        HtmlPage page = j.createWebClient().getPage(run, inputAction.getUrlName());

        j.submit(page.getFormByName(is.getId()), "abort");
        assertEquals(0, inputAction.getExecutions().size());
        queueTaskFuture.get();

        j.assertBuildStatus(Result.ABORTED, j.waitForCompletion(run));

        j.assertLogContains("trying 0", run);
        j.assertLogNotContains("trying 1", run);
        j.assertLogNotContains("trying 2", run);
    }

    @Test
    public void stackTraceOnError() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(
                new CpsFlowDefinition(
                        "def count = 0\n"
                                + "retryExt(retry: 2) {\n"
                                + "  count += 1\n"
                                + "  echo 'Try #' + count\n"
                                + "  if (count == 1) {\n"
                                + "    throw new Exception('foo')\n"
                                + "  }\n"
                                + "  echo 'Done!'\n"
                                + "}\n",
                        true));

        WorkflowRun run = j.buildAndAssertSuccess(p);
        j.assertLogContains("Try #1", run);
        j.assertLogContains("ERROR: Execution failed", run);
        j.assertLogContains("java.lang.Exception: foo", run);
        j.assertLogContains("\tat ", run);
        j.assertLogContains("Try #2", run);
        j.assertLogContains("Done!", run);
    }
}
