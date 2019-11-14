import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Parser {
    private File f;
    private ArrayList<String> instrs;
    private ArrayList<String> instrCodes;
    private HashMap<String, Integer> labelAddresses;

    public Parser(ArrayList<String> instrs, HashMap<String, Integer> labelAddresses, File f) {
        this.instrs = instrs;
        this.labelAddresses = labelAddresses;
        this.f = f;
    }

    public boolean isComment(String s) {
        if (s.startsWith("#") || s.length() == 0) {
            return true;
        }

        return false;
    }

    public ArrayList<PLRegister> thirdPass(ArrayList<String> instructions) {
        ArrayList<PLRegister> plain_text = new ArrayList<PLRegister>();
        for (String s : instructions) {
            String [] instruction_codes = s.split("\\s+");
            String opcode = instruction_codes[0];
            String [] pipeInfo = new String[4];
            PLRegister if_id = new PLRegister("empty");

            if (Utils.isRegisterFormat(opcode)) {
                pipeInfo = Utils.processRegisterFormat(instruction_codes);
                if_id.rs = pipeInfo[0];
                if_id.rt = pipeInfo[1];
                if_id.rd = pipeInfo[2];
                if_id.op = pipeInfo[3];
            } else if (Utils.isJumpFormat(opcode)) {
                pipeInfo = Utils.processJumpFormat(instruction_codes);
                if_id.branchAddress = Integer.parseInt(pipeInfo[0]);
                if_id.op = pipeInfo[3];
            } else {
                pipeInfo = Utils.processImmediateFormat(instruction_codes);
                if_id.rs = pipeInfo[0];
                if_id.rt = pipeInfo[1];
                if_id.branchAddress = Integer.parseInt(pipeInfo[2]);
                if_id.op = pipeInfo[3];
            }
            plain_text.add(if_id);
        }
        return plain_text;
    }

    // Pre: String s is a string which contains a label.
    // Post: This function strips String s's label and add its to the labelAddresses map.
    public void addLabelToLabelAddressMap(String s) {
        String strippedLabel = s.substring(0, s.indexOf(":"));
        this.labelAddresses.put(strippedLabel, this.instrs.size());
    }

    // Post: Gets instruction after label if there is one, otherwise returns an empty string.
    public String getInstructionAfterLabel(String str) {
        int index = str.indexOf(":");

        char[] instructionPart = str.substring(index + 1).toCharArray();

        for (char c : instructionPart) {
            if (c == '#') {
                return str.substring(index + 1, str.indexOf("#")).trim();
            }
        }

        return str.substring(index + 1).trim();
    }

    // Post: Go through every line of assembly & split the contents of the
    // line based on if it is a label or instruction.
    public void performFirstPass() {
        try {
            Scanner inputFileScanner = new Scanner(this.f);
            String currentLineString;
            String instruction;

            while (inputFileScanner.hasNextLine()) {
                currentLineString = inputFileScanner.nextLine().trim();

                if (!isComment(currentLineString)) {
                    if (currentLineString.contains(":")) { // this if statement will be true if a label is found
                        addLabelToLabelAddressMap(currentLineString);
                        instruction = getInstructionAfterLabel(currentLineString);

                        if (!instruction.equals("")) {
                            instrs.add(instruction);
                        }
                    }

                    else if (currentLineString.contains("#")) { // instruction  with a comment
                        currentLineString = currentLineString.substring(0, currentLineString.indexOf("#"));
                        instruction = currentLineString;
                        instrs.add(instruction);
                    }

                    else { // instruction w/o a comment
                        instruction = currentLineString;
                        instrs.add(instruction);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Some sort of error has occurred.");
            System.exit(0);
        }
    }

    public String cleanInstruction(String instr) {
        instr = instr.replace('\t', ' ');
        instr = instr.replace(',', ' ');
        int firstIndexOfDollarSign = instr.indexOf("$");
        if (firstIndexOfDollarSign != -1) {
            instr = instr.substring(0, firstIndexOfDollarSign) + " " + instr.substring(firstIndexOfDollarSign);
        }
        return instr;
    }

    public ArrayList<String> cleanArray(ArrayList<String> a) {
        ArrayList<String> res = new ArrayList<String>();
        for (String element : a) {
            if (element.length() > 0) {
                res.add(element.trim());
            }
        }
        return res;
    }

    // Post: Organizes operands of each instruction into an array which
    // is then passed to the assembler which determines the instruction's format and
    // translates the operands into binary.
    public ArrayList<String> performSecondPass() {
        ArrayList<String> instrCodes = new ArrayList<String>();
        Assembler assembler = new Assembler(this.labelAddresses);
        int lineNumber = 0;
        ArrayList<String> allOperands;
        Assembler a = new Assembler(labelAddresses);

        // split the instruction string into an array where each operand is an element.
        for (String instr : this.instrs) {
            //System.out.println(instr);
            instr = cleanInstruction(instr);

            String[] instrOperands = instr.split(" ");
            allOperands = new ArrayList<String>(Arrays.asList(instrOperands));
            allOperands = cleanArray(allOperands);
            //System.out.println(allOperands);

            if (a.checkIsValidInstruction(instrOperands[0])) {
                String assembledString = assembler.performAssembly(lineNumber, allOperands);
                instrCodes.add(assembledString);
            } else {
                instrCodes.add("invalid instruction: " + instrOperands[0] + "\n");
                return instrCodes;
            }
            lineNumber++;
        }
        return instrCodes;
    }


    public void printStringArray(String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.println("Item: " + arr[i]);
        }
    }
}
