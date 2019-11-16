import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Interpreter {
    private Sim s;
    private Scanner i;
    private boolean interactiveMode;
    private PipelineObject plo;

    // constructor for script mode
    public Interpreter(ArrayList<String> instrCodes, ArrayList<PLRegister> textInstructions, File scriptFile) {
        interactiveMode = false;
        s = new Sim(textInstructions, instrCodes);
        plo = new PipelineObject();
        readScriptFile(scriptFile);
    }

    // constructor for interactive mode
    public Interpreter(ArrayList<String> instrCodes, ArrayList<PLRegister> textInstructions) {
        i = new Scanner(System.in);
        interactiveMode = true;
        s = new Sim(textInstructions, instrCodes);
        plo = new PipelineObject();
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
                int numSteps = 1;
                if (args.length == 2) {
                    numSteps = Integer.parseInt(args[1]);
                }

                for (int i = 0; i < numSteps; i++){
                    int step = s.stepThrough(1);
                    boolean finished = plo.runCycle(s);
                    if (step == 0 || finished) {
                        break;
                    }
                }

                System.out.println(plo);
            }
            else if (args[0].trim().equals("r")) {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    s.stepThrough(1);
                    if (plo.runCycle(s)) {
                        break;
                    }
                }
            }
            else if (args[0].trim().equals("m"))
                s.printMemory(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            else if (args[0].trim().equals("c"))
                s.resetSimulator();
            else if (args[0].trim().equals("q"))
                System.exit(0);
            else if (args[0].trim().equals("p"))
                System.out.println(plo);
            else
                System.out.println(args[0] + " is not a valid command\n");

            System.out.print("mips> ");
        }
    }
}