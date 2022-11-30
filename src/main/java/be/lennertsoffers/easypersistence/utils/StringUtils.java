package be.lennertsoffers.easypersistence.utils;

public final class StringUtils {
    public static String toCamelCase(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    public static String upperToCamelCase(String string) {
        boolean capitalizeNext = false;
        StringBuilder stringBuilder = new StringBuilder();

        char[] characters = string.toCharArray();
        for (char character : characters) {
            if (capitalizeNext) character = Character.toUpperCase(character);
            else character = Character.toLowerCase(character);

            capitalizeNext = character == '_';
            if (!capitalizeNext) stringBuilder.append(character);
        }

        return stringBuilder.toString();
    }

    public static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
