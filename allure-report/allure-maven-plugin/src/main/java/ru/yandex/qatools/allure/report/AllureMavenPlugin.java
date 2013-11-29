package ru.yandex.qatools.allure.report;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import ru.yandex.qatools.allure.data.ReportGenerator;

import java.net.MalformedURLException;
import java.util.*;
import java.io.*;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static ru.yandex.qatools.allure.report.ReportUtils.getReportGeneratorClass;

/**
 * @author Artem Eroshenko eroshenkoam
 *         6/7/13, 6:06 PM
 */
@SuppressWarnings("unused")
@Mojo(name = "allure-maven-plugin", defaultPhase = LifecyclePhase.SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AllureMavenPlugin extends AbstractAllureReportPlugin {

    private static final String TEST_SUITE_FILES_REGEXP = ".*-testsuite\\.xml";

    private static final String TEST_CASE_FILES_REGEXP = ".*-testcase\\.xml";

    private static final String FAQ = "https://github.com/allure-framework/allure/blob/master/docs/FAQ.md";

    private static final String ALLURE_TEAM = "mailto:charlie@yandex-team.ru";

    private StringWriter errorWriter = new StringWriter();

    @Parameter
    private Dependency reportFace = new Dependency(
            "ru.yandex.qatools.allure",
            "allure-report-face",
            "${allure.version}",
            "war"
    );

    @Parameter(defaultValue = "ru.yandex.qatools.allure.data.AllureReportGenerator")
    private String reportGenerator;

    @Parameter(defaultValue = "${project.build.directory}")
    private String outputDirectory;

    @Parameter(defaultValue = "Allure Report")
    private String reportName;

    @Override
    protected final String getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public final File getReportOutputDirectory() {
        return new File(outputDirectory, ALLURE_REPORT_DATA_DIR);
    }

    @Override
    public String getName(Locale locale) {
        return reportName;
    }

    @Override
    protected final void executeReport(Locale locale) throws MavenReportException {
        File reportDirectory = getReportOutputDirectory();

        Sink sink = getSink();

        sink.head();
        sink.title();
        sink.text(getName(locale));
        sink.title_();
        sink.head_();

        sink.body();

        sink.lineBreak();

        if (!(reportDirectory.exists() && reportDirectory.canRead() && reportDirectory.canWrite())) {
            String error = String.format("Sorry, in project \"%s\" no data for the report. To know the reasons why" +
                    " you see this message please see FAQ ", project.getName());
            getLog().error(error + FAQ);
            sink.text(error);
            sink.link(FAQ);
            sink.text(FAQ);
            sink.link_();

        } else if (generateData(reportDirectory) && unpackReport()) {
            sink.rawText("<meta http-equiv=\"refresh\" content=\"0;url=allure-maven-plugin/index.html\" />");
            sink.link("allure-maven-plugin/index.html");
            sink.figure();
            sink.figureGraphics("allure-maven-plugin/img/allure-dashboard.png");
            sink.figure_();
            sink.link_();

        } else {
            sink.figure();
            sink.figureGraphics("allure-maven-plugin/img/report-failed.png");
            sink.figure_();
            sink.lineBreak();
            sink.text(errorWriter.toString());
        }

        sink.body_();

        sink.flush();
        sink.close();
    }

    private boolean generateData(File reportDirectory) {
        try {
            ReportGenerator generator = createReportGeneratorInstance(reportDirectory);
            generator.generate(reportDirectory);

        } catch (ReflectiveOperationException e) {
            getLog().error(String.format(
                    "Can't create instance of report generator %s. %s",
                    reportGenerator,
                    getErrorMessage(e)
            ));
            printStackTrace(e);
            return false;
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            getLog().error(getErrorMessage(e));
            printStackTrace(e);
            return false;
        }
        return true;
    }

    private boolean unpackReport() {
        try {
            executeMojo(
                    getDependencyPlugin(),
                    goal("unpack"),
                    getDependencyPluginConfiguration(),
                    executionEnvironment(getProject(), mavenSession, pluginManager)
            );
        } catch (MojoExecutionException e) {
            getLog().error(getErrorMessage(e));
            printStackTrace(e);
            return false;
        }
        return true;
    }

    private void printStackTrace(Throwable throwable) {
        throwable.printStackTrace(new PrintWriter(errorWriter));
    }

    private String getErrorMessage(Throwable throwable) {
        return String.format(
                "%s: %s. Please, see FAQ %s or contact with allure team %s.",
                throwable.getClass().getName(),
                throwable.getMessage(),
                FAQ,
                ALLURE_TEAM
        );
    }

    private Plugin getDependencyPlugin() {
        return plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.8");
    }

    private Xpp3Dom getDependencyPluginConfiguration() {
        return configuration(
                element(
                        "artifactItems",
                        element("artifactItem",
                                element("groupId", reportFace.getGroupId()),
                                element("artifactId", reportFace.getArtifactId()),
                                element("version", reportFace.getVersion()),
                                element("overWrite", "true"),
                                element("outputDirectory", getOutputDirectory() + "/" + ALLURE_REPORT_DIR),
                                element("type", reportFace.getPackaging())
                        )
                )
        );
    }

    private ReportGenerator createReportGeneratorInstance(File reportDirectory)
            throws MalformedURLException, DependencyResolutionRequiredException, ReflectiveOperationException {

        Class<?> clazz = getReportGeneratorClass(project, reportGenerator);
        return (ReportGenerator) clazz.getDeclaredConstructor(File[].class).newInstance(
                new Object[]{new File[]{reportDirectory}}
        );
    }

}