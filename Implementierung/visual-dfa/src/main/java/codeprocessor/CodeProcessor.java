package codeprocessor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import controller.Controller;

/**
 * A {@code CodeProcessor} represents a unit for compiling and processing
 * java-sourcecode to java-bytecode.
 * 
 * @author Anika Nietzer
 */
public class CodeProcessor {

    private String className = "";
    private String errorMessage = "";
    private String pathName = "";
    private boolean success = false;
    private static final String DEFAULT_CLASS_NAME = "DefaultClass";
    private static final String DEFAULT_CLASS_SIGNATURE = "public class DefaultClass {";
    private static final String DEFAULT_METHOD_SIGNATURE = "public void defaultMethod() {";
    private static final String DEFAULT_TAINT_METHODS = "public static void __taint(Object o) {} "
            + "public static void __taint(boolean b){}" + "public static void __taint(int i){}"
            + "public static void __taint(double d){}" + "public static void __taint(char c){}"
            + "public static void __taint(long l){}" + "public static void __taint(short s){}"
            + "public static void __taint(byte b){}" + "public static void __taint(float f){}";
    private static final String DEFAULT_CLEAN_METHODS = "public void __clean(Object o) {} "
            + "public static void __clean(boolean b){}" + "public static void __clean(int i){}"
            + "public static void __clean(double d){}" + "public static void __clean(char c){}"
            + "public static void __clean(long l){}" + "public static void __clean(short s){}"
            + "public static void __clean(byte b){}" + "public static void __clean(float f){}";
    private static final String DEFAULT_SENSITIVE_METHODS = "public static void __sensitive() {}"
            + "public static void __sensitive(Object o) {}" + "public static void __sensitive(boolean b) {}"
            + "public static void __sensitive(int i) {}" + "public static void __sensitive(double d) {}"
            + "public static void __sensitive(char c) {}" + "public static void __sensitive(long l) {}"
            + "public static void __sensitive(short s) {}" + "public static void __sensitive(byte b) {}"
            + "public static void __sensitive(float f) {}";

    /**
     * Creates a {@code CodeProcessor} with the given code fragment and compiles
     * it to java-bytecode.
     * 
     * @param originalCode
     *            the java-code fragment, that will be compiled
     */
    public CodeProcessor(String originalCode) {
        if (originalCode == null) {
            throw new IllegalArgumentException("String must not be null");
        }
        this.pathName = Controller.getProgramOutputPath() + System.getProperty("file.separator") + "classFiles";
        File dir = new File(this.pathName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String codeToCompile = preProcess(originalCode);
        if (codeToCompile == "") {
            throw new IllegalArgumentException("invalid identifiers");
        } else {
            codeToCompile = codeToCompile.trim();
        }

        // tries to compile the user input
        DiagnosticCollector<JavaFileObject> diagnosticCollector = null;
        boolean containsClass = codeToCompile.contains(" class") || codeToCompile.startsWith("class ");
        if (containsClass) {
            this.className = getClassNameOfCode(codeToCompile);
            if (this.className == "" || this.className == null) {
                return;
            }
            String codeWrap = getTaintWrap(codeToCompile);
            diagnosticCollector = compile(this.className, codeWrap);
        } else {
            String codeWrap = getClassTaintWrap(codeToCompile);
            this.className = DEFAULT_CLASS_NAME;
            diagnosticCollector = compile(this.className, codeWrap);
            if (!this.success) {
                codeWrap = getMethodClassTaintWrap(codeToCompile);
                compile(this.className, codeWrap);
            }
        }

        if (!success) {
            this.errorMessage = diagnosticCollector.getDiagnostics().get(0).toString();
            return;
        }

    }

    private String preProcess(String code) {
        // remove one line comments
        // in Windows
        code = code.replaceAll("//.*?\r\n", "").trim();
        // in Unix, Linux, MAC (OS 10+)
        code = code.replaceAll("//.*?\n", "").trim();
        // in MAC (OS 9-)
        code = code.replaceAll("//.*?\r", "").trim();

        // remove several line comments
        // code =
        // code.replaceAll("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/",
        // "").trim();

        code = code.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "").trim();

        // delete package information
        if (code.startsWith("package")) {
            int i = 0;
            while (!(code.charAt(i) == ';')) {
                i++;
                if (i >= code.length() - 1) {
                    return "";
                }
            }
            code = code.substring(i + 1);
        }
        return code;
    }

    private String getTaintWrap(String code) {
        int endIndex = code.lastIndexOf("}");
        code = code.substring(0, endIndex) + DEFAULT_TAINT_METHODS + DEFAULT_CLEAN_METHODS + DEFAULT_SENSITIVE_METHODS
                + "}";
        return code;
    }

    private String getClassTaintWrap(String code) {
        code = DEFAULT_CLASS_SIGNATURE + code + "}";
        code = getTaintWrap(code);
        return code;
    }

    private String getMethodClassTaintWrap(String code) {
        code = DEFAULT_METHOD_SIGNATURE + code + "}";
        code = getClassTaintWrap(code);
        return code;
    }

    private String getClassNameOfCode(String codeToCompile) {
        int startOfClass = codeToCompile.indexOf("class");
        String tryToFindName = codeToCompile.substring(startOfClass + 5).trim();
        int endOfClassName;
        for (endOfClassName = 0; endOfClassName < tryToFindName.length(); ++endOfClassName) {
            char c = tryToFindName.charAt(endOfClassName);
            if (c == '{' || c == '<' || Character.isWhitespace(c)) {
                break;
            }
        }
        if (endOfClassName >= (tryToFindName.length() - 1)) {
            return null;
        }
        String nameOfClass = tryToFindName.substring(0, endOfClassName).trim();
        return nameOfClass;
    }

    private class StringJavaFileObject extends SimpleJavaFileObject {
        private final CharSequence code;

        private StringJavaFileObject(String name, CharSequence code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private DiagnosticCollector<JavaFileObject> compile(String name, String codeFragment) {
        StringJavaFileObject javaFile = new StringJavaFileObject(name, codeFragment);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollectorLocal = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollectorLocal, null, null);
        Iterable<? extends JavaFileObject> units = Arrays.asList(javaFile);
        try {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(this.pathName)));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Iterable<String> options = Arrays.asList("-g");
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollectorLocal, options,
                null, units);
        this.success = task.call();
        try {
            fileManager.close();
        } catch (IOException e) {
            System.out.println("file manager could not be closed");
        }
        return diagnosticsCollectorLocal;
    }

    /**
     * Returns the name of the compiled class.
     * 
     * @return the className
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Returns an error message, in case of failure.
     * 
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Returns the name of the package of the compiled class.
     * 
     * @return the pathName.
     */
    public String getPath() {
        return this.pathName;
    }

    /**
     * Returns {@code true} if the compiling process was successful or
     * {@code false} in the other case.
     * 
     * @return if the compiling process was successful
     */
    public boolean wasSuccessful() {
        return this.success;
    }
}