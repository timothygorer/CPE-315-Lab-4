

public class PipelineObject{
    private PLRegister ifID;
    private PLRegister idExe;
    private PLRegister exeMem;
    private PLRegister memWb;
    private int pc;
    private int numInstrs;
    private int numCycles;
    private int jumpAddress;
    private boolean completed;
    private boolean stalled;
    private boolean jumped;

    public PipelineObject(){
        this.pc = 0;
        this.numInstrs = 0;
        this.numCycles = 0;
        this.jumpAddress = 0;
        this.ifID = new PLRegister("empty");
        this.idExe = new PLRegister("empty");
        this.exeMem = new PLRegister("empty");
        this.memWb = new PLRegister("empty");
        this.completed = false;
        this.stalled = false;
        this.jumped = false;
    }

    // Post: Prints the PipelineObject.
    public String toString() {
        return "\npc\t if/id\t id/exe\t exe/mem  mem/wb\n"
                + this.pc + "\t " + this.ifID.getOp() + "\t "
                + this.idExe.getOp() + "\t " + this.exeMem.getOp() + "\t    "
                + this.memWb.getOp() + "\n";
    }

    public boolean runCycle(Sim simulator) {
        if (this.completed) {
            return this.completed;
        }

        writeBack(simulator);
        memory();
        execute(simulator);
        decode(simulator);

        if (pc != 0 && this.idExe.getOp().equals("empty") && this.ifID.getOp().equals("empty") &&
                this.exeMem.getOp().equals("empty") && this.memWb.getOp().equals("empty")) {
            System.out.println();
            System.out.println("Program complete");
            System.out.printf("CPI = %.3f", this.numCycles * 1.0 / this.numInstrs);
            System.out.printf("\t Cycles = %d\t ",  this.numCycles);
            System.out.printf("Instructions = %d\n", this.numInstrs);
            this.completed = true;
        }

        return this.completed;
    }

    private void writeBack(Sim simulator) {
        if (this.memWb.getOp().equals("squash") || this.memWb.getOp().equals("stall")) {

        } else if (this.memWb.getOp().equals("empty")) {
            this.numCycles++;
        } else {
            this.numInstrs++;
            this.numCycles++;
        }

        this.memWb = this.exeMem;

        if (this.memWb.getOp().equals("bne") || this.memWb.getOp().equals("beq")) {
            if (simulator.nextBranch()) {
                this.numCycles += 3;
                this.jumped = true;
                this.pc += this.memWb.getBA() - 2;
                this.exeMem = new PLRegister("squash");
                this.idExe = new PLRegister("squash");
                this.ifID = new PLRegister("squash");
            }
        }
    }

    private void memory() {
        if (this.stalled || this.jumped) {
            return;
        }

        this.exeMem = this.idExe;

        if (this.exeMem.getOp().equals("lw") && (this.exeMem.getRt().equals(this.ifID.getRs()) ||
                this.exeMem.getRt().equals(this.ifID.getRt()))) {
            this.stalled = true;
            this.idExe = new PLRegister("stall");
            this.numCycles++;
        }
    }

    private void execute(Sim simulator) {
        if (this.stalled || this.jumped) {
            return;
        }

        this.idExe = this.ifID;
        String op = this.idExe.getOp();
        if (op.equals("j") || op.equals("jal") || op.equals("jr")) {
            this.pc = this.jumpAddress;
            this.jumped = true;
            this.ifID = new PLRegister("squash");
            this.numCycles++;
        }
    }

    private void decode(Sim simulator) {
        if (this.stalled || this.jumped) {
            this.stalled = false;  // reset booleans
            this.jumped = false;
            return;
        }

        String instruction = simulator.getInstruction(pc++);
        if (this.pc > simulator.getInstructionsSize()) {
            this.pc -= 1;
        }
        if (instruction.equals("empty")) {
            this.ifID = new PLRegister("empty");
            return;
        } else {
            this.ifID = new PLRegister(instruction);
        }

        String[] instrCodes = instruction.split("\\s+");
        String opcode = instrCodes[0];
        String[] pipeData = new String[4];

        if (opcode.equals("000000")) { // r format
            pipeData = Utils.processRegisterFormat(instrCodes);
            this.ifID.setRs(pipeData[0]);
            this.ifID.setRt(pipeData[1]);
            this.ifID.setRd(pipeData[2]);
            this.ifID.setOp(pipeData[3]);
        } else if (opcode.equals("000011") || opcode.equals("000010")) { // j format
            pipeData = Utils.processJumpFormat(instrCodes);
            this.ifID.setBA(Integer.parseInt(pipeData[0]));
            this.ifID.setOp(pipeData[3]);
        } else {
            pipeData = Utils.processImmediateFormat(instrCodes);
            this.ifID.setRs(pipeData[0]);
            this.ifID.setRt(pipeData[1]);
            this.ifID.setBA(Integer.parseInt(pipeData[2]));
            this.ifID.setOp(pipeData[3]);
        }

        if (this.ifID.getOp().equals("j") || this.ifID.getOp().equals("jal") || this.ifID.getOp().equals("jr")) {
            if (this.ifID.getRs() != null) {
                this.jumpAddress = simulator.getRegisterValue(this.ifID.getRs());
            } else {
                this.jumpAddress = this.ifID.getBA();
            }
        }
    }
}

class PLRegister {
    private String rd;
    private String rs;
    private String rt;
    private String operation;
    private int branchAddress;
    private String instrCode;
    private int pcCount;

    PLRegister(String operation) {
        this.operation = operation;
    }

    public String getRd() {
        return rd;
    }

    public String getRs() {
        return rs;
    }

    public String getRt() {
        return rt;
    }

    public String getOp() {
        return operation;
    }

    public int getBA() {
        return branchAddress;
    }

    public String getInstrCode() {
        return instrCode;
    }

    public int getPcCount() {
        return pcCount;
    }

    public void setRd(String rd) {
        this.rd = rd;
    }

    public void setRs(String rs) {
        this.rs = rs;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public void setOp(String op) {
        this.operation = op;
    }

    public void setBA(int BA) {
        this.branchAddress = BA;
    }

    public void setInstrCode(String ic) {
        this.instrCode = ic;
    }

    public void setPcCount(int pcCount) {
        this.pcCount = pcCount;
    }

}