package be.lennertsoffers.easypersistence.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EntityColumn {
    private String fieldName;
    private String sqlType;
    private String javaType;
    private String javaFieldName;
    private boolean primaryKey;

    public String getUppercaseName() {
        StringBuilder stringBuilder = new StringBuilder();

        char[] characters = this.fieldName.toCharArray();
        for (char character : characters) {
            if (Character.isUpperCase(character)) stringBuilder.append('_');
            stringBuilder.append(Character.toUpperCase(character));
        }

        return stringBuilder.toString();
    }
}
