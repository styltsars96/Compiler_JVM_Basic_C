package types;

import ast.Operator;
import java.util.Set;
import org.objectweb.asm.Type;
 /**
  *
  * METHOD 11,
  * OBJECT 10,
  * ARRAY 9,
  * DOUBLE 8,
  * LONG 7,
  * FLOAT 6,
  * INT 5,
  * SHORT 4,
  * BYTE 3,
  * CHAR 2,
  * BOOLEAN 1,
  * VOID 0
  **/
public class TypeUtils {

    public static final Type STRING_TYPE = Type.getType(String.class);

    private TypeUtils() {
    }

    /**
     * @param type1
     * @param type2
     * @return The max ASM Type.
     */
    public static Type maxType(Type type1, Type type2) {
        if (type1.equals(STRING_TYPE)) {
            return type1;
        } else if (type2.equals(STRING_TYPE)) {
            return type2;
        } else if (type1.equals(Type.DOUBLE_TYPE)) {
            return type1;
        } else if (type2.equals(Type.DOUBLE_TYPE)) {
            return type2;
        } else if (type1.equals(Type.INT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.INT_TYPE)) {
            return type2;
        }else if (type1.equals(Type.CHAR_TYPE)) {
            return type1;
        } else if (type2.equals(Type.CHAR_TYPE)) {
            return type2;
        } else if (type1.equals(Type.BOOLEAN_TYPE)) {
            return type1;
        } else if (type2.equals(Type.BOOLEAN_TYPE)) {
            return type2;
        } else {
            return type1;
        }
    }

    /**
     * @param type1
     * @param type2
     * @return The min ASM Type.
     */
    public static Type minType(Type type1, Type type2) {
        if (type1.equals(Type.BOOLEAN_TYPE)) {
            return type1;
        } else if (type2.equals(Type.BOOLEAN_TYPE)) {
            return type2;
        }else if (type1.equals(Type.CHAR_TYPE)) {
            return type1;
        } else if (type2.equals(Type.CHAR_TYPE)) {
            return type2;
        } else if (type1.equals(Type.INT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.INT_TYPE)) {
            return type2;
        } else if (type1.equals(Type.DOUBLE_TYPE)) {
            return type1;
        } else if (type2.equals(Type.DOUBLE_TYPE)) {
            return type2;
        } else if (type1.equals(STRING_TYPE)) {
            return type1;
        } else if (type2.equals(STRING_TYPE)) {
            return type2;
        } else {
            return type1;
        }
    }

    /**
     * Is the ASM type1 Larger Or Equal to ASM type2?
     *
     * @param type1
     * @param type2
     * @return Boolean
     */
    public static boolean isLargerOrEqualType(Type type1, Type type2) {
        return type1.getSort() >= type2.getSort();
    }

    /**
     * The type on left of an assignment has to be
     * larger or equal than the one on the right
     * to be Assignable!
     *
     * @param target 'target' =
     * @param source = 'source'
     * @return Boolean
     */
    public static boolean isAssignable(Type target, Type source) {//TODO
        if(source.equals(Type.BOOLEAN_TYPE)){//Target has to always be integer.
          return target.equals(Type.INT_TYPE);
        }
        return isLargerOrEqualType(target, source);
    }

    /**
     * Max type of a SET of types
     *
     * @param types A Set of types
     * @return The max ASM Type.
     */
    public static Type maxType(Set<Type> types) {
        Type max = null;
        for (Type t : types) {
            if (max == null) {
                max = t;
            }
            max = maxType(max, t);
        }
        return max;
    }

    /**
     * Min type of a SET of types
     *
     * @param types A Set of types
     * @return The min ASM Type.
     */
    public static Type minType(Set<Type> types) {
        Type min = null;
        for (Type t : types) {
            if (min == null) {
                min = t;
            }
            min = minType(min, t);
        }
        return min;
    }

    /**
     * Is the operator used for a signed number?
     * i.e. is it like this: -2 or -3.06 ?
     * Or is it for negating a boolean expression?
     * i.e. !true or !i<5
     *
     * @param op The operator.
     * @param type ASM type.
     * @return Boolean
     */
    public static boolean isUnaryComparible(Operator op, Type type) {
        switch (op) {
            case MINUS:
                return isNumber(type);
            case NOT:
                return isBoolean(type);
            default:
                return false;
        }
    }

    /**
     * true if INT or FLOAT (DOUBLE actually)
     *
     * @param type ASM type
     * @return Boolean
     */
    public static boolean isNumber(Type type) {
        return type.equals(Type.INT_TYPE) || type.equals(Type.DOUBLE_TYPE);
    }

    /**
     * true if type EVALUATES TO BOOLEAN
     * (for expression or variable)
     *
     * @param type ASM type
     * @return Boolean
     */
    public static boolean isBoolean(Type type) {
        return type.equals(Type.BOOLEAN_TYPE) || type.equals(Type.INT_TYPE);
    }

    /**
     * Check any of the given types in the set are Numbers.
     * true if INT or FLOAT (DOUBLE actually) is found.
     *
     * @param types a SET of ASM types
     * @return Boolean
     */
    public static boolean isNumber(Set<Type> types) {
        for (Type t : types) {
            if (t.equals(Type.INT_TYPE) || t.equals(Type.DOUBLE_TYPE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the provided operator is unary (- or !) and used
     * appropriately, then the given ASM type is accepted.
     *
     * @param op The operator.
     * @param type ASM type.
     * @return ASM type.
     */
    public static Type applyUnary(Operator op, Type type) throws TypeException {
        if (!op.isUnary()) {
            throw new TypeException("Operator " + op + " is not unary");
        }
        if (!TypeUtils.isUnaryComparible(op, type)) {
            throw new TypeException("Type " + type + " is not unary comparible");
        }
        return type;
    }

    /**
     * If the provided operator is binary and used
     * appropriately, then the evaluated ASM type is accepted.
     * Type evaluation is performed based on operator and ASM types!
     *
     * @param op The operator.
     * @param t1 ASM type of left side.
     * @param t2 ASM type of right side.
     * @return ASM type.
     */
    public static Type applyBinary(Operator op, Type t1, Type t2) throws TypeException {
        if (op.isRelational()) {
            if (TypeUtils.areComparable(t1, t2)) {
                return Type.BOOLEAN_TYPE;
            } else {
                throw new TypeException("Expressions are not comparable");
            }
        /*} else if (op.equals(Operator.PLUS)) {
            return maxType(t1, t2); */
        } else if (op.equals(Operator.PLUS) || op.equals(Operator.MINUS) ||
          op.equals(Operator.DIVISION) || op.equals(Operator.MULTIPLY) || op.equals(Operator.MOD)) {
            /*
            if (t1.equals(TypeUtils.STRING_TYPE) || t2.equals(TypeUtils.STRING_TYPE)) {
                throw new TypeException("Expressions cannot be handled as numbers");
                //NOTE Remember, CHAR CAN be a number, the unicode integer!
            }
            if (t1.equals(Type.BOOLEAN_TYPE) || t2.equals(Type.BOOLEAN_TYPE)) {
                //Boolean cannot be used with non-relational operator!
                throw new TypeException("Expressions cannot be handled as numbers");
            }*/
            if(! ((isNumber(t1)||isBoolean(t1))&&(isNumber(t2)||isBoolean(t2))) ){//Arithmetic ops only with numbers
                throw new TypeException("Only numbers allowed for "+ op +" operation.");
            }
            if( op.equals(Operator.MOD)) {
                if(!(t1.equals(Type.INT_TYPE) && t2.equals(Type.INT_TYPE))){
                  throw new TypeException("Modulus is done only with integers");
                }
            }
            return maxType(t1, t2);
        } else if(op.isBinaryBoolean()){
            if(!areBooleanCompatible(t1, t2)){
              throw new TypeException(op +" is done only with boolean expressions");
            }
            return Type.BOOLEAN_TYPE;
        } else {
            throw new TypeException("Operator " + op + " not supported");
        }

    }

    /**
     * The types provided have to be comparable by using
     * relational operators!
     *
     * @param type1 ASM type of left side.
     * @param type2 ASM type of right side.
     * @return Boolean
     */
    public static boolean areComparable(Type type1, Type type2) {
        if (type1.equals(Type.BOOLEAN_TYPE)) {
            return type2.equals(Type.BOOLEAN_TYPE);
        } else if (type1.equals(Type.INT_TYPE)) {
            return type2.equals(Type.INT_TYPE) || type2.equals(Type.DOUBLE_TYPE);
        } else if (type1.equals(Type.DOUBLE_TYPE)) {
            return type2.equals(Type.INT_TYPE) || type2.equals(Type.DOUBLE_TYPE);
        } else if(type1.equals(Type.CHAR_TYPE)){
            return type2.equals(Type.CHAR_TYPE);
        }else {// string
        	return type2.equals(TypeUtils.STRING_TYPE);
        }
    }

    /**
     * The types provided have to be boolean compatible fi they are
     * integers or booleans!
     *
     * @param type1 ASM type of left side.
     * @param type2 ASM type of right side.
     * @return Boolean
     */
     public static boolean areBooleanCompatible(Type type1, Type type2) {
        if(isBoolean(type1) || type1.equals(Type.INT_TYPE)){
            return isBoolean(type2) || type2.equals(Type.INT_TYPE);
        }else{
            return false;
        }

     }

    /**
     * Check if the given type is a function
     *
     * @param type ASM type of left side.
     * @return true if Function, false if Variable
     */
     public static boolean isFunction(Type type){
        return ( type.toString().indexOf('(') >= 0 );
     }

}
