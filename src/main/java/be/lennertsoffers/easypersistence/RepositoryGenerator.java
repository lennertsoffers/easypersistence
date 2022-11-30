package be.lennertsoffers.easypersistence;

import be.lennertsoffers.easypersistence.models.EntityTable;
import be.lennertsoffers.easypersistence.models.generators.SourceFileGenerator;
import be.lennertsoffers.easypersistence.utils.StringUtils;
import org.apache.velocity.VelocityContext;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

public final class RepositoryGenerator {
    public final static String repositorySuffix = "Repository";

    public static void generate(List<EntityTable> entityTables, ProcessingEnvironment processingEnvironment, String packageName) {
        VelocityContext baseRepositoryContext = new VelocityContext();
        baseRepositoryContext.put("packageName", packageName);
        new SourceFileGenerator(
                "baseRepository.vm",
                "BaseRepository",
                processingEnvironment,
                baseRepositoryContext
        ).generateFile();

        entityTables.forEach(entityTable -> {
            String repositoryName = entityTable.getEntityName() + repositorySuffix;

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("entityTable", entityTable);
            velocityContext.put("packageName", packageName);
            velocityContext.put("repositoryName", repositoryName);
            velocityContext.put("contractName", ContractGenerator.CONTRACT_NAME);
            velocityContext.put("stringUtils", StringUtils.class);

            new SourceFileGenerator(
                    "repositoryTemplate.vm",
                    repositoryName,
                    processingEnvironment,
                    velocityContext
            ).generateFile();
        });
    }
}
