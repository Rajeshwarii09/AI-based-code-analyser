package com.example.codeanalyser.analysis.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.stereotype.Service;

import com.example.codeanalyser.analysis.model.AiInsight;
import com.example.codeanalyser.analysis.model.AnalysisReport;
import com.example.codeanalyser.analysis.model.CodeIssue;
import com.example.codeanalyser.analysis.model.CompilationDiagnostic;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import edu.umd.cs.findbugs.BugCollectionBugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;

@Service
public class CodeAnalysisService {
    private static final Pattern PUBLIC_CLASS = Pattern.compile("\\bpublic\\s+(?:final\\s+|abstract\\s+)?class\\s+([A-Za-z_$][\\w$]*)");

    private final AiAnalysisService aiAnalysisService;

    public CodeAnalysisService(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    public AnalysisReport analyzeCode(String language, String codeSnippet) {
        if (codeSnippet == null || codeSnippet.isBlank()) {
            throw new IllegalArgumentException("Code snippet must not be blank");
        }
        if (codeSnippet.length() > 200_000) {
            throw new IllegalArgumentException("Code snippet must not exceed 200,000 characters");
        }

        List<CodeIssue> issues = new ArrayList<>();
        List<CompilationDiagnostic> diagnostics = new ArrayList<>();
        String normalizedLanguage = normalizeLanguage(language);
        Path workingDirectory = null;
        try {
            workingDirectory = Files.createTempDirectory("code-analyser-");
            Path sourceFile = createSourceFile(workingDirectory, normalizedLanguage, codeSnippet);
            if ("JAVA".equals(normalizedLanguage)) {
                Path classesDirectory = Files.createDirectory(workingDirectory.resolve("classes"));
                compile(sourceFile, classesDirectory, diagnostics);
                runPmd(sourceFile, issues);
                runCheckstyle(sourceFile, issues);
                if (diagnostics.stream().noneMatch(item -> "ERROR".equals(item.getKind()))) {
                    runSpotBugs(classesDirectory, issues);
                }
            } else {
                compileOtherLanguage(normalizedLanguage, sourceFile, workingDirectory, diagnostics);
                runGenericChecks(normalizedLanguage, codeSnippet, issues);
            }
            AiInsight insight = aiAnalysisService.explain(issues, diagnostics);
            String status = diagnostics.stream().anyMatch(item -> "ERROR".equals(item.getKind()))
                ? "COMPLETED_WITH_COMPILATION_ERRORS" : "COMPLETED";
            return new AnalysisReport(status, normalizedLanguage, issues, diagnostics, insight);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not prepare temporary analysis files", exception);
        } finally {
            deleteDirectory(workingDirectory);
        }
    }

    private String normalizeLanguage(String language) {
        String normalized = language == null ? "" : language.trim().toUpperCase();
        if (!List.of("JAVA", "PYTHON", "JAVASCRIPT", "TYPESCRIPT", "C", "CPP", "GO", "RUST")
            .contains(normalized)) {
            throw new IllegalArgumentException(
                "Unsupported language. Choose Java, Python, JavaScript, TypeScript, C, C++, Go, or Rust.");
        }
        return normalized;
    }

    private Path createSourceFile(Path directory, String language, String code) throws IOException {
        if (!"JAVA".equals(language)) {
            return Files.writeString(directory.resolve("Snippet." + extensionFor(language)), code);
        }
        Matcher matcher = PUBLIC_CLASS.matcher(code);
        String className = matcher.find() ? matcher.group(1) : "CodeSnippet";
        return Files.writeString(directory.resolve(className + ".java"), code);
    }

    private String extensionFor(String language) {
        return switch (language) {
            case "PYTHON" -> "py";
            case "JAVASCRIPT" -> "js";
            case "TYPESCRIPT" -> "ts";
            case "C" -> "c";
            case "CPP" -> "cpp";
            case "GO" -> "go";
            case "RUST" -> "rs";
            default -> "java";
        };
    }

    private void compileOtherLanguage(String language, Path source, Path directory,
                                      List<CompilationDiagnostic> diagnostics) {
        List<String> command = switch (language) {
            case "PYTHON" -> List.of("python", "-m", "py_compile", source.toString());
            case "JAVASCRIPT" -> List.of("node", "--check", source.toString());
            case "TYPESCRIPT" -> List.of("tsc", "--noEmit", "--pretty", "false", source.toString());
            case "C" -> List.of("gcc", "-fsyntax-only", source.toString());
            case "CPP" -> List.of("g++", "-fsyntax-only", source.toString());
            case "GO" -> List.of("go", "tool", "compile", "-o", directory.resolve("snippet.o").toString(),
                source.toString());
            case "RUST" -> List.of("rustc", "--emit", "metadata", "-o",
                directory.resolve("snippet.rmeta").toString(), source.toString());
            default -> List.of();
        };
        runCompilerCommand(command, diagnostics);
    }

    private void runCompilerCommand(List<String> command, List<CompilationDiagnostic> diagnostics) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output;
            try (var input = process.getInputStream()) {
                output = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            boolean finished = process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                diagnostics.add(new CompilationDiagnostic("ERROR", 0, 0, "Language compiler timed out."));
            } else if (process.exitValue() != 0) {
                diagnostics.add(new CompilationDiagnostic("ERROR", 0, 0,
                    output.isBlank() ? "Language compiler reported an error." : output.trim()));
            }
        } catch (IOException exception) {
            diagnostics.add(new CompilationDiagnostic("WARNING", 0, 0,
                "Compiler is not installed for this language: " + command.get(0)));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            diagnostics.add(new CompilationDiagnostic("ERROR", 0, 0, "Language compiler was interrupted."));
        }
    }

    private void runGenericChecks(String language, String code, List<CodeIssue> findings) {
        String[] lines = code.split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            if (lines[index].length() > 120) {
                findings.add(new CodeIssue(language, index + 1, "Line exceeds 120 characters.", "warning"));
            }
            if (lines[index].contains("TODO")) {
                findings.add(new CodeIssue(language, index + 1, "TODO comment should be resolved before release.", "warning"));
            }
        }
    }

    private void compile(Path sourceFile, Path classesDirectory,
                         List<CompilationDiagnostic> diagnostics) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            diagnostics.add(new CompilationDiagnostic("ERROR", 0, 0,
                "A JDK is required to compile snippets; no Java compiler is available."));
            return;
        }
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(collector, null, null)) {
            Iterable<? extends JavaFileObject> files = manager.getJavaFileObjects(sourceFile.toFile());
            JavaCompiler.CompilationTask task = compiler.getTask(
                null, manager, collector, List.of("-d", classesDirectory.toString()), null, files);
            task.call();
        } catch (IOException exception) {
            diagnostics.add(new CompilationDiagnostic("ERROR", 0, 0,
                "Compilation could not be started: " + exception.getMessage()));
        }
        for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
            diagnostics.add(new CompilationDiagnostic(
                diagnostic.getKind().name(), diagnostic.getLineNumber(),
                diagnostic.getColumnNumber(), diagnostic.getMessage(null)));
        }
    }

    private void runPmd(Path sourceFile, List<CodeIssue> findings) {
        Path report = null;
        try {
            report = Files.createTempFile("pmd-report-", ".xml");
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setInputPaths(sourceFile.toString());
            configuration.setRuleSets("rulesets/java/quickstart.xml");
            configuration.setReportFormat("xml");
            configuration.setReportFile(report);
            PMD.runPmd(configuration);
            var document = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(report.toFile());
            var violations = document.getElementsByTagName("violation");
            for (int index = 0; index < violations.getLength(); index++) {
                var violation = (org.w3c.dom.Element) violations.item(index);
                findings.add(new CodeIssue("PMD",
                    Integer.parseInt(violation.getAttribute("beginline")),
                    violation.getTextContent().trim(),
                    "1".equals(violation.getAttribute("priority")) ? "error" : "warning"));
            }
        } catch (Exception exception) {
            findings.add(toolFailure("PMD", exception));
        } finally {
            deleteFile(report);
        }
    }

    private void runSpotBugs(Path classesDirectory, List<CodeIssue> findings) {
        try {
            DetectorFactoryCollection.instance();
            var project = new edu.umd.cs.findbugs.Project();
            project.addFile(classesDirectory.toString());
            var reporter = new BugCollectionBugReporter(project);
            FindBugs2 findBugs = new FindBugs2();
            findBugs.setProject(project);
            findBugs.setBugReporter(reporter);
            findBugs.execute();
            for (BugInstance bug : reporter.getBugCollection()) {
                int line = bug.getPrimarySourceLineAnnotation() == null
                    ? 0 : bug.getPrimarySourceLineAnnotation().getStartLine();
                findings.add(new CodeIssue("SpotBugs", line, bug.getMessage(),
                    bug.getPriority() <= 2 ? "error" : "warning"));
            }
        } catch (Exception exception) {
            findings.add(toolFailure("SpotBugs", exception));
        }
    }

    private void runCheckstyle(Path sourceFile, List<CodeIssue> findings) {
        Checker checker = new Checker();
        try {
            var resource = getClass().getClassLoader().getResource("configuration.xml");
            if (resource == null) {
                throw new IllegalStateException("Checkstyle configuration.xml was not found");
            }
            Configuration configuration = ConfigurationLoader.loadConfiguration(
                resource.toExternalForm(), new PropertiesExpander(System.getProperties()));
            checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
            checker.configure(configuration);
            checker.addListener(new AuditListener() {
                public void fileStarted(AuditEvent event) { }
                public void fileFinished(AuditEvent event) { }
                public void auditStarted(AuditEvent event) { }
                public void auditFinished(AuditEvent event) { }
                public void addError(AuditEvent event) {
                    findings.add(new CodeIssue("Checkstyle", event.getLine(), event.getMessage(),
                        event.getSeverityLevel() == SeverityLevel.ERROR ? "error" : "warning"));
                }
                public void addException(AuditEvent event, Throwable throwable) {
                    findings.add(toolFailure("Checkstyle", new Exception(throwable)));
                }
            });
            checker.process(List.of(sourceFile.toFile()));
        } catch (Exception exception) {
            findings.add(toolFailure("Checkstyle", exception));
        } finally {
            checker.destroy();
        }
    }

    private CodeIssue toolFailure(String tool, Exception exception) {
        return new CodeIssue(tool, 0, "Tool execution failed: " + exception.getMessage(), "error");
    }

    private void deleteDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(this::deleteFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not clean temporary analysis files", exception);
        }
    }

    private void deleteFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete temporary analysis file", exception);
        }
    }
}
