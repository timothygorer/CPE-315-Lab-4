import java.util.ArrayList;
import java.util.HashMap;

public class Simulator {
    private int pc;
    private int[] memory; // should be 8192 slots of memory where each slot is 4 bytes
    private int[] registers; // should be 32 slots of registers where each slot is 4 bytes
    private ArrayList<String> instrCodes;
    private HashMap<String, Integer> registerMap; // 5 bit code to register integer map

    public Simulator(ArrayList<String> instrCodes) {
        pc = 0;
        memory = new int[8192];
        registers = new int[32];
        this.registerMap = createRegisterMap();
        this.instrCodes = instrCodes;
    }

    private HashMap<String, Integer> createRegisterMap() {
        HashMap<String, Integer> registerMap = new HashMap<String, Integer>();
        registerMap.put("11111", 31);
        registerMap.put("11110", 30);
        registerMap.put("11101", 29);
        registerMap.put("11100", 28);
        registerMap.put("11011", 27);
        registerMap.put("11010", 26);
        registerMap.put("10111", 23);
        registerMap.put("10110", 22);
        registerMap.put("10101", 21);
        registerMap.put("10100", 20);
        registerMap.put("10011", 19);
        registerMap.put("10010", 18);
        registerMap.put("10001", 17);
        registerMap.put("10000", 16);
        registerMap.put("11001", 25);
        registerMap.put("11000", 24);
        registerMap.put("01111", 15);
        registerMap.put("01110", 14);
        registerMap.put("01101", 13);
        registerMap.put("01100", 12);
        registerMap.put("01011", 11);
        registerMap.put("01010", 10);
        registerMap.put("01001", 9);
        registerMap.put("01000", 8);
        registerMap.put("00111", 7);
        registerMap.put("00110", 6);
        registerMap.put("00101", 5);
        registerMap.put("00100", 4);
        registerMap.put("00011", 3);
        registerMap.put("00010", 2);
        registerMap.put("00001", 1);
        registerMap.put("00000", 0);
        return registerMap;
    }

    // Post: returns the instruction at the current PC as a list of codes
    private String[] getInstructionAtPCAsCodes(){
        return this.instrCodes.get(pc).split(" ");
    }

    // Pre: steps through the program "steps" number of times, executing "steps" number of instructions or until last instruction is met.
    // Post: Returns the number of instructions that were executed.
    public int stepThrough(int steps) {
        int instructionsExecuted = 0;

        for (instructionsExecuted = 0; instructionsExecuted < steps && pc != this.instrCodes.size(); instructionsExecuted++, pc++) {
            String[] codesOfCurrentInstruction = getInstructionAtPCAsCodes();

            if (codesOfCurrentInstruction[0].equals("000010") || codesOfCurrentInstruction[0].equals("000011")) { // if true then this instr. is J type
                //System.out.println("about to interpret j format...");
                interpJFormat(codesOfCurrentInstruction);
            }

            else if (codesOfCurrentInstruction[0].equals("000000")) { // if true then this instr. is R type
                //System.out.println("about to interpret r format...");
                interpRFormat(codesOfCurrentInstruction);
            }

            else { // instr. is I type
                //System.out.println("about to interpret i format...");
                interpIFormat(codesOfCurrentInstruction);
            }
        }

        return instructionsExecuted;
    }

    // Post: Extends "bs" which is a binary number string,
    // to a binary string which is "length" amount of bits in length.
    // Returns the resulting binary number string.
    private String extendBits(String bs, int length) {
        int bsLength = bs.length();
        String mostSignificantBit = bs.substring(0, 1);

        while (bsLength != length) {
            bs = mostSignificantBit + bs;
            bsLength++;
        }
        return bs;
    }

    // Note that I-Format looks as follows: OpCode(6), rs(5), rt(5), immed(16)
    // Post: interprets an I format instruction.
    private void interpIFormat(String[] codesOfCurrentInstruction) {
        String immediateCode = codesOfCurrentInstruction[codesOfCurrentInstruction.length - 1];
        int immediateInt = (int)Long.parseLong(extendBits(immediateCode, 32), 2);

        String opCode = codesOfCurrentInstruction[0]; // opCode of size 6
        int rs = registerMap.get(codesOfCurrentInstruction[1]); // converts a binary string of size 5 to integer
        int rt = registerMap.get(codesOfCurrentInstruction[2]); // converts a binary string of size 5 to integer

        if (opCode.equals("100011")) { // lw case
            //System.out.println("lw");
            registers[rt] = memory[immediateInt + registers[rs]];
        }

        else if (opCode.equals("000101")) { // bne case
            //System.out.println("bne");
            if (registers[rs] != registers[rt]) {
                pc = pc + immediateInt;
            } else {
                pc = pc;
            }
        }

        else if (opCode.equals("001000")) { // addi case
            //System.out.println("addi");
            registers[rt] = registers[rs] + immediateInt;
        }

        else if (opCode.equals("000100")) { // beq case
            //System.out.println("beq");
            if (registers[rs] == registers[rt]) {
                pc = (pc + immediateInt);
            } else {
                pc = pc;
            }
        }

        else if (opCode.equals("101011")) { // sw case
            //System.out.println("sw");
            memory[immediateInt + registers[rs]] = registers[rt];
        }
    }

    // Note that J-Format looks as follows: OpCode(6), Addr(26)
    // Post: interprets a J format instruction.
    private void interpJFormat(String[] codesOfCurrentInstruction) {
        int lineNumber = Integer.parseInt(codesOfCurrentInstruction[1], 2);

        if (codesOfCurrentInstruction[0].equals("000011")) {    // jal stores return address into $ra
            registers[31] = pc + 1;
        }

        pc = lineNumber - 1;  // assign label address to PC
    }

    // Note that R-Format looks as follows: OpCode(6), rs(5), rt(5), rd(5), shamt(5), funct(6)
    // Post: interprets an R format instruction.
    private void interpRFormat(String[] codesOfCurrentInstruction) {
        String functionCode = codesOfCurrentInstruction[codesOfCurrentInstruction.length - 1];

        int rs = registerMap.get(codesOfCurrentInstruction[1]); // converts a binary string of size 5 to integer
        int rt = 0, rd = 0, shamt = 0;

        if (functionCode.equals("001000")) { // if the function code is jr then don't set up rt and rd and shamt
            pc = this.registers[rs] - 1;
            return;
        }

        else { // function code is not jr
            rt = registerMap.get(codesOfCurrentInstruction[2]); // converts a binary string of size 5 to integer
            rd = registerMap.get(codesOfCurrentInstruction[3]); // converts a binary string of size 5 to integer
            shamt = registerMap.get(codesOfCurrentInstruction[4]); // converts a binary string of size 5 to integer
        }

        if (functionCode.equals("100010")) { // sub case
            this.registers[rd] = this.registers[rs] - this.registers[rt];
        }

        else if (functionCode.equals("101010")) { // slt case
            if (this.registers[rs] < this.registers[rt]) {
                this.registers[rd] = 1;
            } else {
                this.registers[rd] = 0;
            }
        }

        else if (functionCode.equals("100101")) { // or case
            this.registers[rd] = this.registers[rs] | this.registers[rt];
        }

        else if (functionCode.equals("100000")) { // add case
            this.registers[rd] = this.registers[rs] + this.registers[rt];
        }

        else if (functionCode.equals("000000")) { // sll case
            this.registers[rd] = this.registers[rt] << shamt;
        }

        else if (functionCode.equals("100100")) { // and case
            this.registers[rd] = this.registers[rs] & this.registers[rt];
        }
    }

    public void printMemory(int start, int end) {
        System.out.println();
        for (int i = start; i <= end; i++) {
            System.out.println("[" +  i + "] = " + memory[i]);
        }
        System.out.println();
    }

    public void printRegisters()
    {
        System.out.println();
        System.out.println("pc = " + pc);
        System.out.println("$0 = " + registers[0] + "\t\t$v0 = " + registers[2] + "\t\t$v1 = " + registers[3] + "\t\t$a0 = " + registers[4]);
        System.out.println("$a1 = " + registers[5] + "\t\t$a2 = " + registers[6] + "\t\t$a3 = " + registers[7] + "\t\t$t0 = " + registers[8]);
        System.out.println("$t1 = " + registers[9] + "\t\t$t2 = " + registers[10] + "\t\t$t3 = " + registers[11] + "\t\t$t4 = " + registers[12]);
        System.out.println("$t5 = " + registers[13] + "\t\t$t6 = " + registers[14] + "\t\t$t7 = " + registers[15] + "\t\t$s0 = " + registers[16]);
        System.out.println("$s1 = " + registers[17] + "\t\t$s2 = " + registers[18] + "\t\t$s3 = " + registers[19] + "\t\t$s4 = " + registers[20]);
        System.out.println("$s5 = " + registers[21] + "\t\t$s6 = " + registers[22] + "\t\t$s7 = " + registers[23] + "\t\t$t8 = " + registers[24]);
        System.out.println("$t9 = " + registers[25] + "\t\t$sp = " + registers[29] + "\t\t$ra = " + registers[31]);
        System.out.println();
    }

    public void showHelp() {
        System.out.println("h = show help");
        System.out.println("d = dump register state");
        System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
        System.out.println("s num = step through num instructions of the program");
        System.out.println("r = run until the program ends");
        System.out.println("m num1 num2 = display data memory from location num1 to num2");
        System.out.println("c = clear all registers, memory, and the program counter to 0");
        System.out.println("q = exit the program");
    }

    public void resetSimulator() {
        this.registers = new int[32];
        this.memory = new int[8192];
        this.pc = 0;
        System.out.println("        Simulator reset\n");
    }
}
