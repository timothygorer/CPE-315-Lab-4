import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class lab4 {
    public static void main(String[] args) {
        try {
            File scriptFile = null;
            File assemblyFile = null;
            Scanner assemblyFileScanner = null;
            ArrayList<String> instrs = new ArrayList<String>();
            ArrayList<String> instrCodes = new ArrayList<String>();
            HashMap<String, Integer> labelAddresses = new HashMap<String, Integer>();
            Interpreter i;

            /* Check correct usage */
            if (args.length == 0 || args.length > 2) {
                System.out.println("Error: Need asm input files");
                System.exit(0);
            }

            if (args.length == 2) { // if true, script mode engaged
                scriptFile = new File(args[1]); // get the script file

                if (!scriptFile.exists()) {
                    System.out.println("Error: File not found");
                    System.exit(0);
                }
            }

            assemblyFile = new File(args[0]);
            if (!assemblyFile.exists()) {
                System.out.println("Error: File not found");
                System.exit(0);
            }

            if (args.length < 1) {
                System.out.println("Error: Need an asm input file");
                return;
            }

            Parser myParser = new Parser(instrs, labelAddresses, assemblyFile);

            myParser.performFirstPass();
            instrCodes = myParser.performSecondPass();

            if (scriptFile != null) {
                i = new Interpreter(instrCodes, scriptFile);
            } else {
                i = new Interpreter(instrCodes);
            }

            i.performInterpretation();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
