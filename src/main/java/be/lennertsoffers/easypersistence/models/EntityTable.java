package be.lennertsoffers.easypersistence.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class EntityTable {
    private final String tableName;
    private final String entityName;
    private final String fullName;
    private final List<EntityColumn> columns = new ArrayList<>();

    public void addColumn(EntityColumn entityColumn) {
        this.columns.add(entityColumn);
    }
}
