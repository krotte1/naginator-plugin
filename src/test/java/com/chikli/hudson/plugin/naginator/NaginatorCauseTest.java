/*
 * The MIT License
 *
 * Copyright (c) 2019 IKEDA Yasuyuki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.chikli.hudson.plugin.naginator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.apache.log4j.Logger;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

/**
 * Tests for {@link NaginatorCause}
 */
public class NaginatorCauseTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static Logger log = Logger.getLogger(NaginatorCauseTest.class);

    /**
     * @return the expected value of "rootURL" in jelly.
     */
    public String getRootURL() {
        return j.contextPath + "/";
    }

    @Test
    public void testCauseLink() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildersList().add(new FailureBuilder());
        p.getPublishersList().add(new NaginatorPublisher(
            "", // regexForRerun
            true,       // rerunIfUnstable
            false,      // rerunMatrixPart
            false,      // checkRegexp
            2,          // maxSchedule
            new FixedDelay(0)   // delay
        ));
        p.scheduleBuild2(0);
        j.waitUntilNoActivity();

        assertEquals(3, p.getLastBuild().getNumber());

        log.info("BEFORE - testCauseLink");
        WebClient wc = j.createWebClient();
        {
            FreeStyleBuild b = p.getBuildByNumber(3);
            FreeStyleBuild causeBuild = p.getBuildByNumber(2);
            HtmlPage page = wc.getPage(b);
            // The page now returns the lineage of what caused the build, with the
            // first being the direct parent. In this case we want to get the last
            List<?> anchors = page.getByXPath("//a[contains(@class,'naginator-cause')]");
            assertNotNull(anchors);
            assertTrue(anchors.get(anchors.size() - 1) instanceof HtmlAnchor);
            HtmlAnchor anchor = (HtmlAnchor) anchors.get(anchors.size() - 1);
            assertNotNull(anchor);
            assertEquals(
                getRootURL() + causeBuild.getUrl(),
                anchor.getHrefAttribute()
            );
        }
        {
            FreeStyleBuild b = p.getBuildByNumber(2);
            FreeStyleBuild causeBuild = p.getBuildByNumber(1);
            HtmlPage page = wc.getPage(b);
            HtmlAnchor anchor = page.getFirstByXPath("//a[contains(@class,'naginator-cause')]");
            assertNotNull(anchor);
            assertEquals(
                getRootURL() + causeBuild.getUrl(),
                anchor.getHrefAttribute()
            );
        }
    }

    @Test
    public void testDisabledCauseLink() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildersList().add(new FailureBuilder());
        p.getPublishersList().add(new NaginatorPublisher(
            "", // regexForRerun
            true,       // rerunIfUnstable
            false,      // rerunMatrixPart
            false,      // checkRegexp
            2,          // maxSchedule
            new FixedDelay(0)   // delay
        ));
        p.scheduleBuild2(0);
        j.waitUntilNoActivity();

        assertEquals(3, p.getLastBuild().getNumber());

        p.getBuildByNumber(1).delete();

        WebClient wc = j.createWebClient();
        {
            FreeStyleBuild b = p.getBuildByNumber(3);
            FreeStyleBuild causeBuild = p.getBuildByNumber(2);
            HtmlPage page = wc.getPage(b);
            HtmlAnchor anchor = page.getFirstByXPath("//a[contains(@class,'naginator-cause')]");
            assertNotNull(anchor);
            assertEquals(
                getRootURL() + causeBuild.getUrl(),
                anchor.getHrefAttribute()
            );
        }
        {
            FreeStyleBuild b = p.getBuildByNumber(2);
            HtmlPage page = wc.getPage(b);
            HtmlAnchor anchor = page.getFirstByXPath("//a[contains(@class,'naginator-cause')]");
            // Change to not null since the second build will have been caused by
            // the first buil that failed
            assertNotNull(anchor);
        }
    }

    @Issue("JENKINS-50751")
    @Test
    public void testCauseLinkWithLargeNumber() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildersList().add(new FailureBuilder());
        p.getPublishersList().add(new NaginatorPublisher(
            "", // regexForRerun
            true,       // rerunIfUnstable
            false,      // rerunMatrixPart
            false,      // checkRegexp
            2,          // maxSchedule
            new FixedDelay(0)   // delay
        ));
        p.updateNextBuildNumber(2000);
        p.scheduleBuild2(0);
        j.waitUntilNoActivity();

        assertEquals(2002, p.getLastBuild().getNumber());

        log.info("BEFORE - testCauseLinkWithLargeNumber");
        WebClient wc = j.createWebClient();
        {
            FreeStyleBuild b = p.getBuildByNumber(2002);
            FreeStyleBuild causeBuild = p.getBuildByNumber(2001);
            HtmlPage page = wc.getPage(b);
            // The page now returns the lineage of what caused the build, with the
            // first being the direct parent. In this case we want to get the last
            List<?> anchors = page.getByXPath("//a[contains(@class,'naginator-cause')]");
            assertNotNull(anchors);
            assertTrue(anchors.get(anchors.size() - 1) instanceof HtmlAnchor);
            HtmlAnchor anchor = (HtmlAnchor) anchors.get(anchors.size() - 1);
            assertNotNull(anchor);
            assertEquals(
                getRootURL() + causeBuild.getUrl(),
                anchor.getHrefAttribute()
            );
        }
        {
            FreeStyleBuild b = p.getBuildByNumber(2001);
            FreeStyleBuild causeBuild = p.getBuildByNumber(2000);
            HtmlPage page = wc.getPage(b);
            HtmlAnchor anchor = page.getFirstByXPath("//a[contains(@class,'naginator-cause')]");
            assertNotNull(anchor);
            assertEquals(
                getRootURL() + causeBuild.getUrl(),
                anchor.getHrefAttribute()
            );
        }
    }

}
