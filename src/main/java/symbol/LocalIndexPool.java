package symbol;

import java.util.SortedSet;
import java.util.TreeSet;
import types.TypeUtils;
import org.objectweb.asm.Type;

/**
 * Helper class to maintain a pool of used-free local variables.
 */
public class LocalIndexPool {

    private final SortedSet<Integer> used;
    private int max;
    private int maxUsed;

    public LocalIndexPool() {
        this(Integer.MAX_VALUE);
    }

    public LocalIndexPool(int max) {
        this.used = new TreeSet<Integer>();
        this.max = max;
        this.maxUsed = 0;
    }

    public int getLocalIndex(Type type) {
        if (type.equals(Type.DOUBLE_TYPE)) {
            return getDoubleLocalIndex();
        } else if (type.equals(Type.INT_TYPE) || type.equals(TypeUtils.STRING_TYPE) || type.equals(Type.CHAR_TYPE) || type.equals(Type.BOOLEAN_TYPE) || type.getDescriptor().contains("[")) {
            return getLocalIndex();
        } else {
            throw new IllegalArgumentException("Not supported type " + type);

        }
    }

    public void freeLocalIndex(int i, Type type) {
        if (type.equals(Type.DOUBLE_TYPE)) {
            freeDoubleLocalIndex(i);
        } else if (type.equals(Type.INT_TYPE) || type.equals(TypeUtils.STRING_TYPE) || type.equals(Type.CHAR_TYPE) || type.equals(Type.BOOLEAN_TYPE) || type.getDescriptor().contains("[")) {
            freeLocalIndex(i);
        } else {
            throw new IllegalArgumentException("Not supported type " + type);
        }
    }

    public int getLocalIndex() {
        for (int i = 0; i < max; i++) {
            if (!used.contains(i)) {
                used.add(i);
                if (i > maxUsed) {
                    maxUsed = i;
                }
                return i;
            }
        }
        throw new RuntimeException("Pool cannot contain more temporaries.");
    }

    public void freeLocalIndex(int t) {
        used.remove(t);
    }

    public int getDoubleLocalIndex() {
        for (int i = 0; i < max; i++) {
            if (!used.contains(i) && !used.contains(i + 1)) {
                used.add(i);
                used.add(i + 1);
                if (i + 1 > maxUsed) {
                    maxUsed = i + 1;
                }
                return i;
            }
        }
        throw new RuntimeException("Pool cannot contain more temporaries.");
    }

    public void freeDoubleLocalIndex(int t) {
        used.remove(t);
        used.remove(t + 1);
    }

    public int getMaxLocals() {
        return maxUsed;
    }

}
