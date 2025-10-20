import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

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

    public void chooseCommandAction(String cmd, String[] args){
        switch(cmd){
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
            terminal.chooseCommandAction(cmd, cmdArgs);
        }
        scanner.close();
    }
};