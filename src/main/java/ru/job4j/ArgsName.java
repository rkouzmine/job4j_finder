package ru.job4j;

import java.util.HashMap;
import java.util.Map;

public class ArgsName {

    private final Map<String, String> values = new HashMap<>();

    public String get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "This key: '%s' is missing", key));
        }
        return values.get(key);
    }

    private void checks(String[] args) {
        for (String arg : args) {
            if (!arg.contains("=")) {
                throw new IllegalArgumentException(String.format(
                        "Error: This argument '%s' does not contain an equal sign", arg));
            }
            if (!arg.startsWith("-")) {
                throw new IllegalArgumentException(String.format(
                        "Error: This argument '%s' does not start with a '-' character", arg));
            }
            if (arg.matches("^-[A-Za-z0-9]+=$")) {
                throw new IllegalArgumentException(String.format(
                        "Error: This argument '%s' does not contain a value", arg));
            }
            if (arg.matches("^-=.*$")) {
                throw new IllegalArgumentException(String.format(
                        "Error: This argument '%s' does not contain a key", arg));
            }
        }
    }

    private void parse(String[] args) {
        checks(args);
        for (String arg : args) {
            var str = arg.split("=", 2);
            var key = str[0];
            var value = str[1];
            values.put(key.substring(1), value);
        }
    }

    public static ArgsName of(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments not passed to program");
        }
        ArgsName names = new ArgsName();
        names.parse(args);
        return names;
    }

}