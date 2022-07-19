package com.ibm.decision.dsa2jaxrs;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.decision.run.provider.ClassLoaderDecisionRunnerProvider;
import com.ibm.decision.run.provider.DecisionRunnerProvider;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/$operation")
public class $className {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name="$operation")
    public $outClass execute($inClass in)
            throws IOException {
        DecisionRunnerProvider provider = new ClassLoaderDecisionRunnerProvider.Builder()
                .build();
        var runner = provider.getDecisionRunner("$operation");

        return ($outClass)runner.execute(in);
    }
}

