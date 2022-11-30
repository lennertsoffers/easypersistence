package be.lennertsoffers.easypersistence;

import be.lennertsoffers.easypersistence.models.EntityTable;
import be.lennertsoffers.easypersistence.models.generators.SourceFileGenerator;
import org.apache.velocity.VelocityContext;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

public final class ContractGenerator {
    public final static String CONTRACT_NAME = "Contract";

    public static void generate(List<EntityTable> entityTables, ProcessingEnvironment processingEnvironment, String packageName) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("entityTables", entityTables);
        velocityContext.put("packageName", packageName);

        new SourceFileGenerator(
                "contractTemplate.vm",
                CONTRACT_NAME,
                processingEnvironment,
                velocityContext
        ).generateFile();
    }
}
