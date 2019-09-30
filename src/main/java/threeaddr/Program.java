package threeaddr;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the list of 3 address code Instructions.
 */
public class Program {

    private final List<Instruction> instructions;
    private long labels;

    public Program() {
        instructions = new ArrayList<Instruction>();
        labels = 1;
    }

    public void add(Instruction instruction) {
        instructions.add(instruction);
    }

    public Instruction get(int i) {
        return instructions.get(i);
    }

    public void set(int i, Instruction instruction) {
        instructions.set(i, instruction);
    }

    public void clear() {
        instructions.clear();
    }

    public LabelInstr addNewLabel() {
        String name = "L" + Long.toString(labels++);
        LabelInstr i = new LabelInstr(name);
        instructions.add(i);
        return i;
    }

    public static void backpatch(List<GotoInstr> list, LabelInstr target) {
        if (list == null) {
            return;
        }
        for (GotoInstr instr : list) {
            instr.setTarget(target);
        }
    }

    public String emit() {
        StringBuilder sb = new StringBuilder();
        for (Instruction i : instructions) {
            sb.append(i.emit());
            sb.append("\n");
        }
        return sb.toString();
    }

}
