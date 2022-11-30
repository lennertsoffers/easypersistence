package be.lennertsoffers.easypersistence;

import be.lennertsoffers.easypersistence.models.EntityTable;
import be.lennertsoffers.easypersistence.models.generators.SourceFileGenerator;
import be.lennertsoffers.easypersistence.utils.StringUtils;
import org.apache.velocity.VelocityContext;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.List;

public final class DatabaseGenerator {
    public static void generate(List<EntityTable> entityTables, ProcessingEnvironment processingEnvironment, String packageName, String databaseName, int databaseVersion) {
        VelocityContext databaseContext = new VelocityContext();
        databaseContext.put("packageName", packageName);
        databaseContext.put("databaseName", databaseName);
        databaseContext.put("databaseVersion", databaseVersion);
        databaseContext.put("contractName", ContractGenerator.CONTRACT_NAME);

        new SourceFileGenerator(
                "databaseHelper.vm",
                "DatabaseHelper",
                processingEnvironment,
                databaseContext
        ).generateFile();

        databaseContext.put("entityTables", entityTables);
        databaseContext.put("repositorySuffix", RepositoryGenerator.repositorySuffix);
        databaseContext.put("stringUtils", StringUtils.class);

        new SourceFileGenerator(
                "easyPersistenceDatabase.vm",
                "EasyPersistenceDatabase",
                processingEnvironment,
                databaseContext
        ).generateFile();
    }
}
