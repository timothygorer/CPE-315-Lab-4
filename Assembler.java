import java.util.ArrayList;
import java.util.HashMap;

public class Assembler {
    private HashMap<String, String> registers = createRegisterMap();
    private HashMap<String, Integer> labelAddresses;
    private HashMap<String, String> functionCodes = createFunctionCodeMap();
    private HashMap<String, String> opCodes = createOpCodeMap();

    public Assembler(HashMap<String, Integer> labelAddresses) {
        this.labelAddresses = labelAddresses;
    }

    public boolean checkIsValidInstruction(String opCode) {
        if (opCodes.containsKey(opCode)) {
            return true;
        }
        return false;
    }

    public String performAssembly(int lineNumber, ArrayList<String> operands) {
        String firstOperand = operands.get(0);

        if (functionCodes.containsKey(operands.get(0))) {       // only R-format instructions have
            return processRegisterFormatToBinary(operands);    // functions codes
        } else if (operands.size() >= 3) {                    // I-formats have 3 or more instr
            return processImmediateFormatToBinary(lineNumber, operands);
        } else {
            return processJumpFormatToBinary(operands);
        }
    }

    // Pre: A list of operands of the form: OpCode(6), rs(5), rt(5), immed(16)
    public String processImmediateFormatToBinary(int lineNumber, ArrayList<String> operands) {
        String bs = "";
        String bits = "";
        String opCode = operands.get(0);
        bs += opCodes.get(opCode) + " ";

        if (operands.size() != 3) {
            if (labelAddresses.containsKey(operands.get(3))) { // branches
                bits = Integer.toBinaryString(this.labelAddresses.get(operands.get(3)) -
                        1 - lineNumber);
                if (bits.length() == 32) {
                    bits = bits.substring(16);
                }
                bits = String.format("%16s", bits).replace(' ', '0');
                bs += this.registers.get(operands.get(1)) + " ";
                bs += this.registers.get(operands.get(2)) + " ";
                bs += bits;
            } else { // immediate arith. operations
                bs += this.registers.get(operands.get(2)) + " ";
                bs += this.registers.get(operands.get(1)) + " ";
                bits = Integer.toBinaryString(Integer.parseInt(operands.get(3)));
                if (bits.length() == 32) {
                    bits = bits.substring(16);
                }
                bits = String.format("%16s", bits).replace(' ', '0');
                bs += bits;
            }
        } else {
            int indexOfParen = operands.get(2).indexOf("(");
            String v = operands.get(2).substring(0, indexOfParen);
            int c = Integer.parseInt(v);
            bits = String.format("%16s", Integer.toBinaryString(c)).replace(' ', '0');

            if (bits.length() == 32) {
                bits = bits.substring(16);
            }

            bits = String.format("%16s", bits).replace(' ', '0');
            bs += this.registers.get(operands.get(2).substring(indexOfParen + 1, operands.get(2).length() - 1)) + " ";
            bs += this.registers.get(operands.get(1)) + " ";
            bs += bits;
        }

        return bs;
    }

    // Pre: A list of operands of the form: Opcode (6 bits), rs (5 bits), rt (5 bits), rd (5 bits), shamt (5 bits), funct (6 bits)
    public String processRegisterFormatToBinary(ArrayList<String> operands) {
        String bs = "";
        String bits = "";
        String opCode = operands.get(0);
        String binaryFormOfOperation = this.opCodes.get(opCode);
        bs += (binaryFormOfOperation + " ");

        if (opCode.equals("sll")) {
            bits = Integer.toBinaryString(Integer.parseInt(operands.get(3)));
            if (bits.length() == 32) {
                bits = bits.substring(16);
            }
            bits = String.format("%5s", bits).replace(' ', '0');
            bs += "00000 ";
            bs += this.registers.get(operands.get(2)) + " ";
            bs += this.registers.get(operands.get(1)) + " ";
            bs += bits + " ";
        } else if (opCode.equals("or") || opCode.equals("add") || opCode.equals("and")
                || opCode.equals("slt") || opCode.equals("sub")) {
            bs += this.registers.get(operands.get(2)) + " ";
            bs += this.registers.get(operands.get(3)) + " ";
            bs += this.registers.get(operands.get(1)) + " ";
            bs += "00000 ";
        } else {
            bs += this.registers.get(operands.get(1)) + " ";
            bs += "000000000000000 ";
        }

        bs += this.functionCodes.get(opCode);
        return bs.toString();
    }

    // Pre: A list of operands of the form: OpCode(6), Addr(26)
    public String processJumpFormatToBinary(ArrayList<String> operands) {
        String bs = "";
        String operation = operands.get(0);
        String twentySixBitAddr = Integer.toBinaryString(this.labelAddresses.get(operands.get(1)));
        if (twentySixBitAddr.length() == 32) {
            twentySixBitAddr = twentySixBitAddr.substring(16);
        }
        twentySixBitAddr = String.format("%26s", twentySixBitAddr).replace(' ', '0');
        bs += this.opCodes.get(operation) + " ";
        bs += twentySixBitAddr;
        return bs;
    }


    private HashMap<String, String> createRegisterMap() {
        HashMap<String, String> rm = new HashMap<String, String>();
        rm.put("$k0", "11010");
        rm.put("$ra", "11111");
        rm.put("$gp", "11100");
        rm.put("$k1", "11011");
        rm.put("$fp", "11110");
        rm.put("$sp", "11101");
        rm.put("$t0", "01000");
        rm.put("$t1", "01001");
        rm.put("$t2", "01010");
        rm.put("$t3", "01011");
        rm.put("$t4", "01100");
        rm.put("$t5", "01101");
        rm.put("$t6", "01110");
        rm.put("$t7", "01111");
        rm.put("$t8", "11000");
        rm.put("$t9", "11001");
        rm.put("$0", "00000");
        rm.put("$zero", "00000");
        rm.put("$a0", "00100");
        rm.put("$a1", "00101");
        rm.put("$a2", "00110");
        rm.put("$a3", "00111");
        rm.put("$v0", "00010");
        rm.put("$v1", "00011");
        rm.put("$at", "00001");
        rm.put("$s0", "10000");
        rm.put("$s1", "10001");
        rm.put("$s2", "10010");
        rm.put("$s3", "10011");
        rm.put("$s4", "10100");
        rm.put("$s5", "10101");
        rm.put("$s6", "10110");
        rm.put("$s7", "10111");
        return rm;
    }

    private HashMap<String, String> createFunctionCodeMap() {
        HashMap<String, String> fcm = new HashMap<String, String>();
        fcm.put("and", "100100");
        fcm.put("or", "100101");
        fcm.put("add", "100000");
        fcm.put("slt", "101010");
        fcm.put("jr", "001000");
        fcm.put("sll", "000000");
        fcm.put("sub", "100010");
        return fcm;
    }

    private HashMap<String, String> createOpCodeMap() {
        HashMap<String, String> om = new HashMap<String, String>();
        om.put("and", "000000");
        om.put("or", "000000");
        om.put("add", "000000");
        om.put("addi", "001000");
        om.put("j", "000010");
        om.put("jr", "000000");
        om.put("jal", "000011");
        om.put("lw", "100011");
        om.put("sll", "000000");
        om.put("sub", "000000");
        om.put("slt", "000000");
        om.put("beq", "000100");
        om.put("bne", "000101");
        om.put("sw", "101011");
        return om;
    }
}
