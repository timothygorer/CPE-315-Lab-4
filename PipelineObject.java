class PLRegister {
    String rd;
    String rs;
    String rt;
    String instrCode;
    int pc;
    String op;
    int branchAddress;

    PLRegister(String op) {
        this.op = op;
    }
}

public class PipelineObject {
    private PLRegister if_id;
    private PLRegister id_exe;
    private PLRegister exe_mem;
    private PLRegister mem_wb;
    private int pc;
    private boolean finished;
    private boolean stalled;
    private boolean jumped;
    private int numberOfInstructions;
    private int numberOfCycles;
    private int jumpAddress;

    public PipelineObject() {
        this.pc = 0;
        this.numberOfInstructions = 0;
        this.numberOfCycles = 0;
        this.jumpAddress = 0;
        this.finished = false;
        this.stalled = false;
        this.jumped = false;
        this.if_id = new PLRegister("empty");
        this.id_exe = new PLRegister("empty");
        this.exe_mem = new PLRegister("empty");
        this.mem_wb = new PLRegister("empty");
    }

    // Post: Prints the PipelineObject.
    public String toString() {
        return "\npc\t if/id\t id/exe\t exe/mem  mem/wb\n"
                + this.pc + "\t " + this.if_id.op + "\t "
                + this.id_exe.op + "\t " + this.exe_mem.op + "\t    "
                + this.mem_wb.op + "\n";
    }

    private void write_back(Sim simulator) {
        if (!(this.mem_wb.op.equals("stall")) && !(this.mem_wb.op.equals("squash"))) {
            if (!this.mem_wb.op.equals("empty")) {
                this.numberOfInstructions++;
            }
            this.numberOfCycles++;
        }

        this.mem_wb = this.exe_mem;

        if (this.mem_wb.op.equals("beq") || this.mem_wb.op.equals("bne")) {
            if (simulator.nextBranch()) {
                this.numberOfCycles += 3;
                jumped = true;
                this.pc += this.mem_wb.branchAddress - 2;
                exe_mem = new PLRegister("squash");
                id_exe = new PLRegister("squash");
                if_id = new PLRegister("squash");
            }
        }
    }

    private void memory() {
        if (this.stalled || this.jumped) {
            return;
        }

        this.exe_mem = this.id_exe;

        if (this.exe_mem.op.equals("lw") && (
                this.exe_mem.rt.equals(this.if_id.rt) ||
                        this.exe_mem.rt.equals(this.if_id.rs))) {
            this.stalled = true;
            this.id_exe = new PLRegister("stall");
            this.numberOfCycles++;
        }
    }

    private void execute(Sim simulator) {
        if (this.jumped || this.stalled) {
            return;
        }

        this.id_exe = this.if_id;

        if (this.id_exe.op.equals("jal") || this.id_exe.op.equals("j") || this.id_exe.op.equals("jr")) {
            this.pc = this.jumpAddress;
            this.jumped = true;
            this.if_id = new PLRegister("squash");
            this.numberOfCycles++;
        }
    }

    // Note that we do nothing if the last instruction was a jump or we stalled.
    private void decode(Sim simulator) {
        if (this.stalled || this.jumped) {
            this.stalled = false;
            this.jumped = false;
            return;
        }

        /* Check if no more instructions to run */
        System.out.println(this.pc);
        String instruction = simulator.getInstruction(this.pc++);

        if (this.pc > simulator.instructionsSize()) {
            this.pc--;
        }
        if (instruction.equals("empty")) {
            if_id = new PLRegister("empty");
            return;
        } else {
            if_id = new PLRegister(instruction);
        }

        /* Parse Instruction Codes */
        String[] instruction_codes = instruction.split("\\s+");
        String opcode = instruction_codes[0];
        String[] pipeInfo = new String[4];

        if (Utils.isRegisterFormat(opcode)) {
            pipeInfo = Utils.processRegisterFormat(instruction_codes);
            this.if_id.rs = pipeInfo[0];
            this.if_id.rt = pipeInfo[1];
            this.if_id.rd = pipeInfo[2];
            this.if_id.op = pipeInfo[3];
        } else if (Utils.isJumpFormat(opcode)) {
            pipeInfo = Utils.processJumpFormat(instruction_codes);
            this.if_id.branchAddress = Integer.parseInt(pipeInfo[0]);
            this.if_id.op = pipeInfo[3];
        } else {
            pipeInfo = Utils.processImmediateFormat(instruction_codes);
            this.if_id.rs = pipeInfo[0];
            this.if_id.rt = pipeInfo[1];
            this.if_id.branchAddress = Integer.parseInt(pipeInfo[2]);
            this.if_id.op = pipeInfo[3];
        }

        if (this.if_id.op.equals("j") || this.if_id.op.equals("jal") || this.if_id.op.equals("jr")) {
            if (this.if_id.rs == null) {
                this.jumpAddress = this.if_id.branchAddress;
            } else {
                this.jumpAddress = simulator.getRegisterValue(if_id.rs);
            }
        }
    }

    public boolean runOneCycle(Sim s) {
        if (finished) {
            return finished;
        }

        write_back(s);
        memory();
        execute(s);
        decode(s);

        if (this.if_id.op.equals("empty") && this.id_exe.op.equals("empty") &&
                this.exe_mem.op.equals("empty") && this.mem_wb.op.equals("empty")
                && this.pc != 0) {
            finished = true;
            System.out.println("Program complete");
            System.out.printf("CPI = %.3f\t Cycles = %d\t Instructions = %d\n", this.numberOfCycles * 1.0 / this.numberOfInstructions,
                    this.numberOfCycles, this.numberOfInstructions);
        }

        return finished;
    }
}
