package com.ibm.decision.dsa2jaxrs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.ibm.decision.run.DecisionServiceProvider;
import com.ibm.decision.run.provider.ClassLoaderDecisionRunnerProvider;

@Mojo( name = "dsa2jaxrs", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES )
public class DSA2JAXRS extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project = null;
    
    private static int opId;
    
    private ClassLoaderDecisionRunnerProvider getProvider(URL dsaURL) {
        ClassLoaderDecisionRunnerProvider provider =  new ClassLoaderDecisionRunnerProvider.Builder()
                .withClassLoader(new URLClassLoader(new URL[] {
                    dsaURL    
                }, DecisionServiceProvider.class.getClassLoader()))
                .build();
        
        return provider;
    }
    
    public void execute(Artifact artifact) throws IOException {
        ClassLoaderDecisionRunnerProvider p = getProvider(artifact.getFile().toURI().toURL());
        
        var ops = p.getDecisionOperations();
        System.out.println(artifact.getFile() + " operations=" + ops);
        
        for (String op: ops)
            execute(p, op);
    }

    public void execute(ClassLoaderDecisionRunnerProvider provider, String op) throws IOException {
        var runner = provider.getDecisionRunner(op);
                
        var inType = runner.getInputType().getTypeName();
        var outType = runner.getOutputType().getTypeName();
        var providerClass = provider.getClass().getName();

        InputStream in = getClass().getClassLoader().getResourceAsStream("com/ibm/decision/dsa2jaxrs/DecisionResource.java");
        
        File d = new File("src/main/java/com/ibm/decision/dsa2jaxrs");
        Files.createDirectories(d.toPath());

        var className = "DecisionResource" + opId;
        opId++;
        File f = new File(d, className + ".java");
        f.delete();

        try (FileWriter out = new FileWriter(f)) {
            try (InputStreamReader isr = new InputStreamReader(in)) {
                BufferedReader reader = new BufferedReader(isr);
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replace("$operation", op);
                    line = line.replace("$providerClass", providerClass);
                    line = line.replace("$className", className);
                    line = line.replace("$inClass", inType);
                    line = line.replace("$outClass", outType);
                    out.write(line + "\n");
                }
            }
        }
    }
    
    public void execute() throws MojoExecutionException {
        for (final Object o : project.getDependencyArtifacts()) {
            try {
                execute((Artifact)o);
            } catch(IOException e) {
                throw new MojoExecutionException("Failed to generate the resource", e);
            }
        }
    }
}

