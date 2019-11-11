import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Interpreter {
    private Simulator s;
    private Scanner i;
    private boolean interactiveMode;

    // constructor for script mode
    public Interpreter(ArrayList<String> instrCodes, File scriptFile) {
        interactiveMode = false;
        s = new Simulator(instrCodes);
        readScriptFile(scriptFile);
    }

    // constructor for interactive mode
    public Interpreter(ArrayList<String> instrCodes) {
        i = new Scanner(System.in);
        interactiveMode = true;
        s = new Simulator(instrCodes);
    }

    private void readScriptFile(File s) {
        try {
            i = new Scanner(s);
        } catch(Exception exception) {
            System.exit(0);
        }
    }

    // Post: Runs interpreter unless user quits ('q')
    public void performInterpretation() {
        System.out.print("mips> ");

        while (this.i.hasNextLine()) {
            String line = i.nextLine();
            String[] args =  line.split(" ");

            if (!interactiveMode) {
                System.out.println(line);
            }

            if (args[0].trim().equals("h"))
                s.showHelp();
            else if (args[0].trim().equals("d"))
                s.printRegisters();
            else if (args[0].equals("s")) {
                int steps;
                if (args.length != 2) {
                    steps = 1;
                } else {
                    steps = Integer.parseInt(args[1]);
                }
                System.out.println("       " + s.stepThrough(steps) + " instruction(s) executed\n");
            }
            else if (args[0].trim().equals("r"))
                s.stepThrough(Integer.MAX_VALUE);
            else if (args[0].trim().equals("m"))
                s.printMemory(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            else if (args[0].trim().equals("c"))
                s.resetSimulator();
            else if (args[0].trim().equals("q"))
                System.exit(0);
            else
                System.out.println(args[0] + " is not a valid command\n");

            System.out.print("mips> ");
        }
    }
}
