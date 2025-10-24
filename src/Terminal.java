import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;

class Parser{
    private String commandName;
    private String[] args;

    public boolean parse(String input){
        if(input == null || input.trim().isEmpty()){
            return false;
        }
        String[] parts = input.trim().split("\\s+");
        if(parts.length == 0){
            args = new String[0];
            return true;
        }
        commandName = parts[0];
        args = new String[parts.length-1];
        for(int i = 1 ; i < parts.length ; i++){
            args[i-1] = parts[i];
        }
        return true;
    }
    public String getCommandName(){
        return commandName;
    }
    public String[] getArgs(){
        return args;
    }
};

public class Terminal{
    Parser parser = new Parser();
    private Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

    public String pwd(){
        return currentDir.toString();
    }

    public void cd(String[] args){
        try{
            if(args == null || args.length == 0){
                Path home = Paths.get(System.getProperty("user.home"));
                currentDir = home.toAbsolutePath().normalize();
                return;
            }
            if(args.length == 1){
                String target = args[0];
                if("..".equals(target)){
                    Path parent = currentDir.getParent();
                    if(parent != null){
                        currentDir = parent.toAbsolutePath().normalize();
                    }
                    return;
                }
                Path path = Paths.get(target);
                if(!path.isAbsolute()){
                    path = currentDir.resolve(path);
                }
                path = path.toAbsolutePath().normalize();
                if(Files.exists(path) && Files.isDirectory(path)){
                    currentDir = path;
                }
                else{
                    System.out.println("Wrong Directory: " + path);
                }
                return;
            }
            System.out.println("cd: too many arguments");
        } catch(Exception e){
            System.out.println("cd "+e.getMessage());
        }
    }

    public String ls(){
        try{
            List<String> files = new ArrayList<>();
            if(currentDir.toFile().listFiles() == null){
                return null;
            }
            for(File file : currentDir.toFile().listFiles()){
                files.add(file.getAbsoluteFile().getName());
            }
            files.sort(String.CASE_INSENSITIVE_ORDER);
            for(String file : files){
                System.out.println(file);
            }
            return String.join(", ", files);
        }
        catch(Exception e){
            System.out.println("ls: " + e.getMessage());
        }
        return null;
    }

    public void mkdir(String[] args){
        if(args == null || args.length == 0){
            System.out.println("mkdir: No arguments provided");
            return;
        }
        for(String arg : args){
            try{
                Path path = Paths.get(arg);
                if(!path.isAbsolute()){
                    path = currentDir.resolve(path);
                }
                Files.createDirectory(path);
            }
            catch(Exception e){
                System.out.println("mkdir: "+e.getMessage());
            }
        }
    }

    public void rmdir(String[] args){
        if(args == null || args.length == 0){
            System.out.println("rmdir: No arguments provided");
            return;
        }
        String target = args[0];
        try{
            if("*".equals(target)){
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)){
                    for(Path path : stream){
                        if(Files.isDirectory(path)){
                            try{
                                Files.delete(path);
                            } catch(IOException IOE){
                                System.out.println("rmdir: Couldn't delete "+IOE.getMessage());
                            }
                        }
                    }
                }
                return;
            }
            Path path = Paths.get(target);
            if(!path.isAbsolute()){
                path = currentDir.resolve(path);
            }
            path = path.toAbsolutePath().normalize();
            if(!Files.exists(path)){
                System.out.println("rmdir: No such a directory: "+path);
                return;
            }
            if(!Files.isDirectory(path)){
                System.out.println("rmdir: Not a directory: "+path);
                return;
            }
            try{
                Files.delete(path);
            } catch(IOException ioe){
                System.out.println("rmdir: Couldn't delete "+ioe.getMessage());
            }
        }catch(Exception e){
            System.out.println("rmdir: "+e.getMessage());
        }
    }

    public void touch(String[] args){
        if(args == null || args.length == 0){
            System.out.println("touch: No arguments provided");
            return;
        }
        try{
            Path path = Paths.get(args[0]);
            if(!path.isAbsolute()){
                path = currentDir.resolve(path);
            }
            path = path.toAbsolutePath().normalize();
            Path parent = path.getParent();
            if(parent != null && !Files.exists(parent)){
                Files.createDirectory(parent);
            }
            if(!Files.exists(path)){
                Files.createFile(path);
            }

        }
        catch(Exception e){
            System.out.println("touch: "+e.getMessage());
        }
    }

    public void rm(String[] args){
        if(args == null || args.length == 0){
            System.out.println("rm: No arguments provided");
            return;
        }
        String target = args[0];
        Path path = Paths.get(target);
        if(!path.isAbsolute()){
            path = currentDir.resolve(path);
        }
        path = path.toAbsolutePath().normalize();

        try{
            if(!Files.exists(path)){
                System.out.println("rm: No such a file: "+path);
                return;
            }

            if(Files.isDirectory(path)){
                System.out.println("rm: Not a file: "+path);
                return;
            }
            Files.delete(path);
        }
        catch(Exception e){
            System.out.println("rm: "+e.getMessage());
        }
    }

    public void cat(String[] args) {
        if (args.length == 0 || args.length > 2) {
            System.out.println("Wrong Input!!");
            return;
        }

        for (String fileName : args) {
            Path path = Paths.get(fileName);
            if (!path.isAbsolute()) {
                path = currentDir.resolve(path);
            }
            path = path.toAbsolutePath().normalize();

            if (!Files.exists(path)) {
                System.err.println("cat: " + fileName + ": No such file");
                continue;
            }

            try {
                Files.lines(path).forEach(System.out::println);
            } catch (IOException e) {
                System.err.println("cat: " + fileName + ": " + e.getMessage());
            }
        }
    }

    public void wc(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: wc <filename>");
            return;
        }
        Path path = Paths.get(args[0]);
        if (!path.isAbsolute()) {
            path = currentDir.resolve(path);
        }
        path = path.toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            System.err.println("wc: " + args[0] + ": No such file");
            return;
        }
        try {
            long lineCount = 0, wordCount = 0, charCount = 0;

            for (String line : Files.readAllLines(path)) {
                lineCount++;
                charCount += line.length();
                wordCount += line.trim().isEmpty() ? 0 : line.trim().split("\\s+").length;
            }

            System.out.println(lineCount + " " + wordCount + " " + charCount + " " + path.getFileName());

        } catch (IOException e) {
            System.err.println("wc: " + args[0] + ": " + e.getMessage());
        }
    }

    public void zip(String[] args) {
        try {
            boolean recursive = false;
            String zipFileName;
            List<String> filesToZip = new ArrayList<>();

            if (args.length >= 3 && args[0].equals("-r")) {
                recursive = true;
                zipFileName = args[1];
                filesToZip.add(args[2]);
            } else if (args.length >= 2) {
                zipFileName = args[0];
                filesToZip.addAll(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
            } else {
                System.out.println("Usage: zip [-r] archive.zip file(s)/directory");
                return;
            }

            Path zipPath = Paths.get(zipFileName);
            try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (String fileName : filesToZip) {
                    Path filePath = Paths.get(fileName);
                    if (Files.isDirectory(filePath) && recursive) {
                        Files.walk(filePath).forEach(path -> {
                            try {
                                if (!Files.isDirectory(path)) {
                                    ZipEntry zipEntry = new ZipEntry(filePath.relativize(path).toString());
                                    zos.putNextEntry(zipEntry);
                                    Files.copy(path, zos);
                                    zos.closeEntry();
                                }
                            } catch (IOException e) {
                                System.err.println("Error adding file: " + path);
                            }
                        });
                    } else if (Files.isRegularFile(filePath)) {
                        ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
                        zos.putNextEntry(zipEntry);
                        Files.copy(filePath, zos);
                        zos.closeEntry();
                    } else {
                        System.err.println("File not found: " + fileName);
                    }
                }
            }
            System.out.println("Created: " + zipPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error creating ZIP: " + e.getMessage());
        }
    }

    public void unzip(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: unzip archive.zip [-d destination]");
            return;
        }

        String zipFileName = args[0];
        String destDir = ".";

        if (args.length >= 3 && args[1].equals("-d")) {
            destDir = args[2];
        }

        Path destPath = Paths.get(destDir);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFilePath = destPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    Files.createDirectories(newFilePath.getParent());
                    Files.copy(zis, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
            System.out.println("Extracted to: " + destPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error extracting ZIP: " + e.getMessage());
        }
    }

    public void chooseCommandAction(String cmd, String[] args){
        switch(cmd){
            case "echo":
                if (args == null || args.length == 0) {
                    System.out.println();
                } else {
                    System.out.println(String.join(" ", args));
                }
                break;
            case "pwd":
                System.out.println(pwd());
                break;
            case "cd":
                cd(args);
                break;
            case "ls":
                ls();
                break;
            case "mkdir":
                mkdir(args);
                break;
            case "rmdir":
                rmdir(args);
                break;
            case "touch":
                touch(args);
                break;
            case "rm":
                rm(args);
                break;
            case "cat":
                cat(args);
                break;
            case "wc":
                wc(args);
                break;
            case "zip":
                zip(args);
                break;
            case "unzip":
                unzip(args);
                break;
            default:
                System.out.println("Unknown command: " + cmd);
        }
    }

    public static void main(String[] args){
        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print("> ");
            String line;
            try{
                line = scanner.nextLine();
            }catch(NoSuchElementException e){
                break;
            }
            if(line == null){
                break;
            }
            line = line.trim();
            if(line.isEmpty()){
                continue;
            }
            if("exit".equals(line)){
                break;
            }
            if(!terminal.parser.parse(line)){
                continue;
            }

            String cmd = terminal.parser.getCommandName();
            String[] cmdArgs = terminal.parser.getArgs();

            // 🔽 ADD FROM HERE
            if(line.contains(">")) {
                boolean append = line.contains(">>");
                String[] parts = append ? line.split(">>") : line.split(">");
                if(parts.length == 2) {
                    String left = parts[0].trim();
                    String right = parts[1].trim();

                    // Parse left command
                    if(!terminal.parser.parse(left)) continue;
                    cmd = terminal.parser.getCommandName();
                    cmdArgs = terminal.parser.getArgs();

                    // Capture output
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    java.io.PrintStream ps = new java.io.PrintStream(baos);
                    java.io.PrintStream oldOut = System.out;
                    System.setOut(ps);

                    terminal.chooseCommandAction(cmd, cmdArgs);

                    System.out.flush();
                    System.setOut(oldOut);

                    String output = baos.toString();
                    java.nio.file.Path outFile = java.nio.file.Paths.get(right);
                    if(!outFile.isAbsolute()){
                        outFile = terminal.currentDir.resolve(outFile);
                    }
                    outFile = outFile.toAbsolutePath().normalize();

                    try {
                        if(append)
                            java.nio.file.Files.write(outFile, output.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                        else
                            java.nio.file.Files.write(outFile, output.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (Exception e) {
                        System.out.println("Redirection error: " + e.getMessage());
                    }
                    continue;
                }
            }




            terminal.chooseCommandAction(cmd, cmdArgs);
        }
        scanner.close();
    }
};
