import java.io.File;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Parser {
    private File f;
    private ArrayList<String> instrCodes;
    private HashMap<String, Integer> labelAddresses;
    private String[] pipeData;

    public Parser(ArrayList<String> instrCodes, HashMap<String, Integer> labelAddresses, File f) {
        this.instrCodes = instrCodes;
        this.labelAddresses = labelAddresses;
        this.f = f;
        this.pipeData = new String[4];
    }

    public boolean isComment(String s) {
        if (s.startsWith("#") || s.length() == 0) {
            return true;
        }

        return false;
    }

    public ArrayList<PLRegister> performPipeline(ArrayList<String> instructions) {
        ArrayList<PLRegister> plainText = new ArrayList<PLRegister>();
        for (int i = 0; i < instructions.size(); i++) {
            String[] instrCodes = instructions.get(i).split("\\s+");
            String oc = instrCodes[0];
            PLRegister ifID = new PLRegister("empty");

            if (isRegisterFormat(oc)) {
                pipeData = processRegisterFormat(instrCodes);
                ifID.setRs(pipeData[0]);
                ifID.setRt(pipeData[1]);
                ifID.setRd(pipeData[2]);
                ifID.setOp(pipeData[3]);
            } else if (isJumpFormat(oc)) {
                pipeData = processJumpFormat(instrCodes);
                ifID.setBA(Integer.parseInt(pipeData[0]));
                ifID.setOp(pipeData[3]);
            } else {
                pipeData = processImmediateFormat(instrCodes);
                ifID.setRs(pipeData[0]);
                ifID.setRt(pipeData[1]);
                ifID.setBA(Integer.parseInt(pipeData[2]));
                ifID.setOp(pipeData[3]);
            }

            plainText.add(ifID);
        }

        return plainText;
    }

    // Pre: String s is a string which contains a label.
    // Post: This function strips String s's label and add its to the labelAddresses map.
    public void addLabelToLabelAddressMap(String s) {
        String strippedLabel = s.substring(0, s.indexOf(":"));
        this.labelAddresses.put(strippedLabel, this.instrCodes.size());
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
                            instrCodes.add(instruction);
                        }
                    }

                    else if (currentLineString.contains("#")) { // instruction  with a comment
                        currentLineString = currentLineString.substring(0, currentLineString.indexOf("#"));
                        instruction = currentLineString;
                        instrCodes.add(instruction);
                    }

                    else { // instruction w/o a comment
                        instruction = currentLineString;
                        instrCodes.add(instruction);
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
        for (String instr : this.instrCodes) {
            instr = cleanInstruction(instr);
            String[] instrOperands = instr.split(" ");
            allOperands = new ArrayList<String>(Arrays.asList(instrOperands));
            allOperands = cleanArray(allOperands);

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

    private static String getRegisterFormatOp(String functionCode) {
        if (functionCode.equals("100010")) { // sub case
            return "sub";
        }

        else if (functionCode.equals("101010")) { // slt case
            return "slt";
        }

        else if (functionCode.equals("100101")) { // or case
            return "or";
        }

        else if (functionCode.equals("100000")) { // add case
            return "add";
        }

        else if (functionCode.equals("000000")) { // sll case
            return "sll";
        }

        else if (functionCode.equals("100100")) { // and case
            return "and";
        }

        return "jr";
    }

    public static boolean isRegisterFormat(String oc) {
        if (oc.equals("000000")) {
            return true;
        }

        return false;
    }

    public static boolean isJumpFormat(String oc) {
        if (oc.equals("000011") || oc.equals("000010")) {
            return true;
        }

        return false;
    }

    public String[] processImmediateFormat(String[] instrCodes) {
        this.pipeData[2] = "" + (int) Long.parseLong(extendBits(instrCodes[instrCodes.length - 1], 32), 2);
        this.pipeData[1] = instrCodes[2];
        this.pipeData[0] = instrCodes[1];

        if (instrCodes[0].equals("001000")) {
            this.pipeData[3] = "addi";
        } else if (instrCodes[0].equals("000100")) {
            this.pipeData[3] = "beq";
        } else if (instrCodes[0].equals("000101")) {
            this.pipeData[3] = "bne";
        } else if (instrCodes[0].equals("100011")) {
            this.pipeData[3] = "lw";
        } else {
            this.pipeData[3] = "sw";
        }

        return this.pipeData;
    }

    // post: Returns a string array of length 4 where index 0 holds rs, index 1 holds rt, index 2 holds rd, and index 3 holds the op name.
    public static String[] processRegisterFormat(String[] instrCodes) {
        String[] pipeData = new String[4];
        pipeData[0] = instrCodes[1];

        pipeData[3] = getRegisterFormatOp(instrCodes[instrCodes.length - 1]);

        if (instrCodes.length > 4) {

            pipeData[2] = instrCodes[3];
            pipeData[1] = instrCodes[2];
        }

        return pipeData;
    }

    public String[] processJumpFormat(String[] instrCodes) {
        if (instrCodes[0].equals("000010")) {
            this.pipeData[3] = "j";
        } else {
            this.pipeData[3] = "jal";
        }

        int line = Integer.parseInt(instrCodes[1], 2);
        this.pipeData[0] = "" + line;
        return this.pipeData;
    }

    // Post: Extends "bs" which is a binary number string,
    // to a binary string which is "length" amount of bits in length.
    // Returns the resulting binary number string.
    private static String extendBits(String bs, int length) {
        int bsLength = bs.length();
        String mostSignificantBit = bs.substring(0, 1);

        while (bsLength != length) {
            bs = mostSignificantBit + bs;
            bsLength++;
        }
        return bs;
    }
}
