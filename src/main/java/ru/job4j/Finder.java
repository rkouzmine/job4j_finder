package ru.job4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Finder {

    private static final Logger LOG = LoggerFactory.getLogger(Finder.class.getName());

    public static void main(String[] args) {
        LOG.info("The Finder program has started ");
        try {
            validationArgs(args);
            LOG.info("Arguments successfully validated: {}", (Object) args);

            ArgsName argsName = ArgsName.of(args);
            Path dir = Path.of(argsName.get("d"));
            String fileName = argsName.get("n");
            String type = argsName.get("t");
            Path fileOutput = Path.of(argsName.get("o"));

            List<Path> list = search(dir, searchType(fileName, type));
            LOG.info("Files found: {}", list.size());

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileOutput.toFile(), true)))) {
                for (Path file : list) {
                    out.println(file.toString());
                }
            }
            LOG.info("Results successfully written.");
        } catch (IOException e) {
            LOG.error("Error writing to file: {}", e.getMessage(), e);
        } finally {
            LOG.info("The Finder program has finished working");
        }
    }

    public static List<Path> search(Path root, Predicate<Path> condition) {
        SearchFiles searcher = new SearchFiles(condition);
        try {
            Files.walkFileTree(root, searcher);
            LOG.info("Search in directory {} completed successfully", root);
        } catch (IOException e) {
            LOG.error("Error traversing the file system: {}", e.getMessage(), e);
            throw new RuntimeException("Error searching for files", e);
        }
        return searcher.getPaths();
    }

    public static Predicate<Path> searchType(String name, String type) {
        LOG.info("Determining search type");
        if ("name".equals(type)) {
            return path -> path.getFileName().toString().equals(name);
        } else if ("mask".equals(type)) {
            Pattern pattern = Pattern.compile(name.replace("?", "\\S{1}").replace("*", "\\S*"));
            return path -> pattern.matcher(path.getFileName().toString()).matches();
        } else if ("regex".equals(type)) {
            Pattern pattern = Pattern.compile(name);
            return path -> path.getFileName().toString().matches(pattern.pattern());
        }
        String message = String.format("Error determining search type: {}:", name);
        LOG.error(message);
        throw new IllegalArgumentException(message);
    }

    private static void validationArgs(String[] args) {
        try {
            ArgsName values = ArgsName.of(args);
            if (args.length != 4) {
                throw new IllegalArgumentException("Not all arguments are specified");
            }
            Path dir = Path.of(values.get("d"));
            if (!Files.isDirectory(dir)) {
                throw new IllegalArgumentException(
                        String.format("The specified directory '%s' does not exist", dir.toAbsolutePath()));
            }
            if (values.get("n").trim().isEmpty()) {
                throw new IllegalArgumentException("File name not specified");
            }
            String type = values.get("t");
            if (!type.equals("name") && !type.equals("mask") && !type.equals("regex")) {
                throw new IllegalArgumentException(
                        String.format("Invalid search type: %s. Available types: name, mask, regex", type));
            }
            Path fileOutput = Path.of(values.get("o"));
            if (!fileOutput.toString().matches("^.*\\.[a-z0-9]{2,5}$")) {
                throw new IllegalArgumentException(
                        String.format("Incorrect file format: %s", fileOutput.getFileName()));
            }

        } catch (IllegalArgumentException e) {
            LOG.error("Error validating arguments: {}", e.getMessage(), e);
            throw e;
        }
    }

}