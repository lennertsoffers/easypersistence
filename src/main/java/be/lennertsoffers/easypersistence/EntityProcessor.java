package be.lennertsoffers.easypersistence;

import com.google.auto.service.AutoService;
import be.lennertsoffers.easypersistence.annotations.*;
import be.lennertsoffers.easypersistence.models.EntityColumn;
import be.lennertsoffers.easypersistence.models.EntityTable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@SupportedAnnotationTypes({"be.lennertsoffers.easypersistence.annotations.Entity", "be.lennertsoffers.easypersistence.annotations.Configure", "be.lennertsoffers.easypersistence.annotations.Database"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class EntityProcessor extends AbstractProcessor {
    private boolean configured = false;
    private String packageName = null;
    private String databaseName = null;
    private Integer databaseVersion = null;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> configurationElements = roundEnv.getElementsAnnotatedWith(Configure.class);
        Set<? extends Element> entityElements = roundEnv.getElementsAnnotatedWith(Entity.class);

        if (this.configured && !configurationElements.isEmpty()) throw new RuntimeException("You can only have one Configure annotation");
        if (!entityElements.isEmpty()) {
            if (!this.configured) {
                if (configurationElements.size() < 1) throw new RuntimeException("You need to configure easyPersistence by adding the Configuration annotation");

                Configure configuration = configurationElements.iterator().next().getAnnotation(Configure.class);
                String packageName = configuration.packageLocation();
                if (packageName.isBlank()) throw new RuntimeException("You cannot have an empty package location in your configuration");
                String databaseName = configuration.databaseName();
                if (databaseName.isBlank()) throw new RuntimeException("You cannot have an empty database name in your configuration");

                this.configured = true;
                this.packageName = packageName;
                this.databaseName = databaseName;
                this.databaseVersion = configuration.databaseVersion();
            }

            List<EntityTable> entityTables = entityElements
                    .stream()
                    .map(element -> {
                        String tableName = element.getAnnotation(Entity.class).tableName();
                        if (tableName.isBlank()) tableName = element.getSimpleName().toString();
                        EntityTable entityTable = new EntityTable(tableName, element.getSimpleName().toString(), this.processingEnv.getElementUtils().getPackageOf(element).getQualifiedName() + "." + element.getSimpleName());

                        AtomicBoolean foundPrimaryKey = new AtomicBoolean(false);
                        element.getEnclosedElements().forEach(enclosedElement -> {
                            if (enclosedElement.getKind().equals(ElementKind.FIELD)) {
                                boolean primaryKey = enclosedElement.getAnnotation(PrimaryKey.class) != null;
                                if (primaryKey) foundPrimaryKey.set(true);

                                Column columnAnnotation = enclosedElement.getAnnotation(Column.class);
                                if (columnAnnotation == null && !primaryKey) return;

                                String columnName;
                                if (columnAnnotation == null || columnAnnotation.name().isBlank()) {
                                    columnName = enclosedElement.getSimpleName().toString();
                                } else {
                                    columnName = columnAnnotation.name();
                                }

                                TypeMirror typeMirror = enclosedElement.asType();
                                String type = null;
                                String sqlType;
                                String javaType;
                                // Convert the java type to the type used in SQLite
                                // Primitive: Kind of type mirror
                                // Type: Cast to declared type and get SimpleName
                                if (typeMirror.getKind().isPrimitive()) type = typeMirror.getKind().toString().toLowerCase();
                                if (typeMirror.getKind().equals(TypeKind.DECLARED)) {
                                    DeclaredType declaredType = (DeclaredType) typeMirror;
                                    type = declaredType.asElement().getSimpleName().toString().toLowerCase();
                                }

                                if (type == null) throw new RuntimeException("Cannot find type of " + enclosedElement.getSimpleName().toString());
                                switch (type) {
                                    case "int" -> {
                                        sqlType = "INTEGER";
                                        javaType = "Integer";
                                    }
                                    case "double" -> {
                                        sqlType = "DECIMAL";
                                        javaType = "Double";
                                    }
                                    case "string" -> {
                                        sqlType = "TEXT";
                                        javaType = "String";
                                    }
                                    default -> throw new RuntimeException("Unsupported type '" + type + "'");
                                }

                                entityTable.addColumn(new EntityColumn(columnName, sqlType, javaType, enclosedElement.getSimpleName().toString(), primaryKey));
                            }
                        });

                        if (!foundPrimaryKey.get()) throw new RuntimeException("You have not provided a primary key for " + element.getSimpleName().toString());

                        return entityTable;
                    })
                    .toList();

            ContractGenerator.generate(entityTables, this.processingEnv, this.packageName);
            RepositoryGenerator.generate(entityTables, this.processingEnv, this.packageName);
            DatabaseGenerator.generate(entityTables, this.processingEnv, this.packageName, this.databaseName, this.databaseVersion);
        }

        return true;
    }
}
