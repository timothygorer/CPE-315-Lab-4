public class Utils {
    private static String[] pipeData = new String[4];

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
        int last_element = instrCodes.length - 1;
        pipeData[2] = "" + (int) Long.parseLong(extendBits(instrCodes[last_element], 32), 2);
        pipeData[1] = instrCodes[2];
        pipeData[0] = instrCodes[1];

        if (instrCodes[0].equals("001000")) {
            pipeData[3] = "addi";
        } else if (instrCodes[0].equals("000100")) {
            pipeData[3] = "beq";
        } else if (instrCodes[0].equals("000101")) {
            pipeData[3] = "bne";
        } else if (instrCodes[0].equals("100011")) {
            pipeData[3] = "lw";
        } else {
            pipeData[3] = "sw";
        }

        return pipeData;
    }

    // post: Returns a string array of length 4 where index 0 holds rs, index 1 holds rt, index 2 holds rd, and index 3 holds the op name.
    public static String[] processRegisterFormat(String[] instrCodes) {
        String[] pipeData = new String[4];
        int last_index = instrCodes.length - 1;
        pipeData[0] = instrCodes[1];
        if (instrCodes.length > 4) {
            pipeData[1] = instrCodes[2];
            pipeData[2] = instrCodes[3];
        }
        pipeData[3] = getRegisterFormatOperation(instrCodes[last_index]);
        return pipeData;
    }

    public static String[] processJumpFormat(String[] instrCodes) {
        if (instrCodes[0].equals("000010")) {
            pipeData[3] = "j";
        } else {
            pipeData[3] = "jal";
        }

        int line = Integer.parseInt(instrCodes[1], 2);
        pipeData[0] = "" + line;
        return pipeData;
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
