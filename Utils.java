public class Utils {
    private static String getRegisterFormatOperation(String functionCode) {
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
        return (oc.equals("000000"));
    }

    public static boolean isJumpFormat(String oc) {
        return oc.equals("000011") || oc.equals("000010");
    }

    public static String[] processImmediateFormat(String[] instrCodes) {
        String [] pipeInfo = new String[4];
        String opcode = instrCodes[0];
        int last_element = instrCodes.length - 1;
        pipeInfo[0] = instrCodes[1];
        pipeInfo[1] = instrCodes[2];
        pipeInfo[2] = "" + (int) Long.parseLong(extendBits(instrCodes[last_element], 32), 2);
        if (opcode.equals("001000")) {
            pipeInfo[3] = "addi";
        } else if (opcode.equals("000100")) {
            pipeInfo[3] = "beq";
        } else if (opcode.equals("000101")) {
            pipeInfo[3] = "bne";
        } else if (opcode.equals("100011")) {
            pipeInfo[3] = "lw";
        } else {
            pipeInfo[3] = "sw";
        }
        return pipeInfo;
    }

    // post: Returns a string array of length 4 where index 0 holds rs, index 1 holds rt, index 2 holds rd, and index 3 holds the op name.
    public static String[] processRegisterFormat(String[] instrCodes) {
        String[] pipeInfo = new String[4];
        int last_index = instrCodes.length - 1;
        pipeInfo[0] = instrCodes[1];
        if (instrCodes.length > 4) {
            pipeInfo[1] = instrCodes[2];
            pipeInfo[2] = instrCodes[3];
        }
        pipeInfo[3] = getRegisterFormatOperation(instrCodes[last_index]);
        return pipeInfo;
    }

    public static String[] processJumpFormat(String[] instrCodes) {
        String[] pipeInfo = new String[4];
        if (instrCodes[0].equals("000010")) {
            pipeInfo[3] = "j";
        } else {
            pipeInfo[3] = "jal";
        }
        int line_number = ~Integer.parseInt(instrCodes[1], 2);
        pipeInfo[0] = "" + line_number;
        return pipeInfo;
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
