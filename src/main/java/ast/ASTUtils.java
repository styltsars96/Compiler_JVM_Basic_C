package ast;

import java.util.ArrayList;
import java.util.List;
import symbol.LocalIndexPool;
import org.objectweb.asm.Type;
import java.util.Collection;
import symbol.SymTable;
import symbol.SymTableEntry;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * Class with static helper methods for AST handling
 */
public class ASTUtils {

	public static final String SYMTABLE_PROPERTY = "SYMTABLE_PROPERTY";
	public static final String LOCAL_INDEX_POOL_PROPERTY = "LOCAL_INDEX_POOL_PROPERTY";
	public static final String IS_BOOLEAN_EXPR_PROPERTY = "IS_BOOLEAN_EXPR_PROPERTY";
	public static final String TYPE_PROPERTY = "TYPE_PROPERTY";
    public static final String NEXT_LIST_PROPERTY = "NEXT_LIST_PROPERTY";
	public static final String BREAK_LIST_PROPERTY = "BREAK_LIST_PROPERTY";
	public static final String CONTINUE_LIST_PROPERTY = "CONTINUE_LIST_PROPERTY";
	public static final String TRUE_LIST_PROPERTY = "TRUE_LIST_PROPERTY";
	public static final String FALSE_LIST_PROPERTY = "FALSE_LIST_PROPERTY";

	private ASTUtils() {
	}

	/**
	 * Get the symbol table, when it SURELY EXISTS.
	 *
	 * @param node
	 *            The AST node.
	 * @return Reference to the corresponding symbol table.
	 */
	@SuppressWarnings("unchecked")
	public static SymTable<SymTableEntry> getEnv(ASTNode node) {
		return (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
	}

	/**
	 * Get the symbol table, without being sure if it exits.
	 *
	 * @param node
	 *            The AST node.
	 * @return Reference to the corresponding symbol table.
	 */
	@SuppressWarnings("unchecked")
	public static SymTable<SymTableEntry> getSafeEnv(ASTNode node) throws ASTVisitorException {
		SymTable<SymTableEntry> symTable = (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
		if (symTable == null) {
			ASTUtils.error(node, "Symbol table not found.");
		}
		return symTable;
	}

	/**
	 * Set the corresponding symbol table to the node.
	 *
	 * @param node
	 *            The AST node.
	 * @param env
	 *            The symbol table.
	 */
	public static void setEnv(ASTNode node, SymTable<SymTableEntry> env) {
		node.setProperty(SYMTABLE_PROPERTY, env);
	}

	public static void setLocalIndexPool(ASTNode node, LocalIndexPool pool) {
		node.setProperty(LOCAL_INDEX_POOL_PROPERTY, pool);
	}

	@SuppressWarnings("unchecked")
	public static LocalIndexPool getSafeLocalIndexPool(ASTNode node)
			throws ASTVisitorException {
		LocalIndexPool lip = (LocalIndexPool) node.getProperty(LOCAL_INDEX_POOL_PROPERTY);
		if (lip == null) {
			ASTUtils.error(node, "Local index pool not found.");
		}
		return lip;
	}
	/**
	 * Is it, or does it have a Boolean Expression?
	 * Careul with its use!
	 *
	 * @param node
	 *            The AST node, an Expression!
	 * @return If truly a boolean expression.
	 */
	public static boolean isBooleanExpression(Expression node) {
		Boolean b = (Boolean) node.getProperty(IS_BOOLEAN_EXPR_PROPERTY);
		if (b == null) {
			return false;
		}
		return b;
	}

	/**
	 * Qualify a node as a Boolean Expression.
	 *
	 * @param node
	 *            The AST node, an Expression!
	 * @param value
	 *            Is it truly a boolean?
	 */
	public static void setBooleanExpression(Expression node, boolean value) {
		node.setProperty(IS_BOOLEAN_EXPR_PROPERTY, value);
	}

	/**
	 * Get the type of the node, if it is SURELY set!
	 *
	 * @param node
	 *            The AST node, an Expression!
	 * @return The type property of the node.
	 */
	public static Type getType(ASTNode node) {
		return (Type) node.getProperty(TYPE_PROPERTY);
	}

	/**
	 * Get the type of the node!
	 *
	 * @param node
	 *            The AST node, an Expression!
	 * @return The type property of the node.
	 */
	public static Type getSafeType(ASTNode node) throws ASTVisitorException {
		Type type = (Type) node.getProperty(TYPE_PROPERTY);
		if (type == null) {
			ASTUtils.error(node, "Type not found.");
		}
		return type;
	}

	/**
	 * Set the ASM type of the AST node.
	 *
	 * @param node
	 *            Any AST node!
	 * @param type
	 *            An ASM Type!
	 */
	public static void setType(ASTNode node, Type type) {
		node.setProperty(TYPE_PROPERTY, type);
	}

	/**
	 * Get the list of goto instructions, when the node's expression evaluates to true.
	 * @param node
	 *						The AST node, an Expression with boolean property!
	 * @return
	 *				The list of goto instructions.
	 */
	@SuppressWarnings("unchecked")
	public static List<JumpInsnNode> getTrueList(Expression node) {
		List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(TRUE_LIST_PROPERTY);
		if (l == null) {
			l = new ArrayList<JumpInsnNode>();
			node.setProperty(TRUE_LIST_PROPERTY, l);
		}
		return l;
	}

	/**
	 * For boolean expressions in if or while etc.
	 * Keep a list of goto instructions, if the expression evaluates to true.
	 *
	 * @param node
	 *            The AST node, an Expression with boolean property!
	 * @param list
	 *            The list of goto instructions.
	 */
  public static void setTrueList(Expression node, List<JumpInsnNode> list) {
    node.setProperty(TRUE_LIST_PROPERTY, list);
  }

	/**
	 * Get the list of goto instructions, when the node's expression evaluates to false.
	 * @param node
	 *						The AST node, an Expression with boolean property!
	 * @return
	 *				The list of goto instructions.
	 */
	@SuppressWarnings("unchecked")
	public static List<JumpInsnNode> getFalseList(Expression node) {
		List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(FALSE_LIST_PROPERTY);
		if (l == null) {
			l = new ArrayList<JumpInsnNode>();
			node.setProperty(FALSE_LIST_PROPERTY, l);
		}
		return l;
	}

	/**
	 * For boolean expressions in if or while etc.
	 * Keep a list of goto instructions, if the expression evaluates to false.
	 *
	 * @param node
	 *            The AST node, an Expression with boolean property!
	 * @param list
	 *            The list of goto instructions.
	 */
  public static void setFalseList(Expression node, List<JumpInsnNode> list) {
    node.setProperty(FALSE_LIST_PROPERTY, list);
  }
	/**
	 * Get the list of goto instructions for what follows after the statement/expression.
	 * @param node
	 *            The AST node, a Statement!
	 * @return
	 *        The list of goto instructions.
	 */
	@SuppressWarnings("unchecked")
	public static List<JumpInsnNode> getNextList(Statement node) {
		List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(NEXT_LIST_PROPERTY);
		if (l == null) {
			l = new ArrayList<JumpInsnNode>();
			node.setProperty(NEXT_LIST_PROPERTY, l);
		}
		return l;
	}

	/**
	 * For statements like if while etc.
	 * Keep a list of goto instructions, for what follows after the statement/expression.
	 *
	 * @param node
	 *            The AST node, a Statement!
	 * @param list
	 *            The list of goto instructions.
	 */
	public static void setNextList(Statement node, List<JumpInsnNode> list) {
		node.setProperty(NEXT_LIST_PROPERTY, list);
	}

	/**
	 * Get the list of goto instructions for the break statement.
	 * @param node
	 *            The AST node, a break Statement!
	 * @return
	 *        The list of goto instructions.
	 */
	@SuppressWarnings("unchecked")
	public static List<JumpInsnNode> getBreakList(Statement node) {
		List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(BREAK_LIST_PROPERTY);
		if (l == null) {
			l = new ArrayList<JumpInsnNode>();
			node.setProperty(BREAK_LIST_PROPERTY, l);
		}
		return l;
	}

	/**
	 * For break statements inside a while.
	 * Keep a list of goto instructions, for when it appears.
	 *
	 * @param node
	 *            The AST node, a Break Statement!
	 * @param list
	 *            The list of goto instructions.
	 */
  public static void setBreakList(Statement node, List<JumpInsnNode> list) {
  	node.setProperty(BREAK_LIST_PROPERTY, list);
  }

	/**
	 * Get the list of goto instructions for the continue statement.
	 * @param node
	 *            The AST node, a continue Statement!
	 * @return
	 *        The list of goto instructions.
	 */
	@SuppressWarnings("unchecked")
	public static List<JumpInsnNode> getContinueList(Statement node) {
		List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(CONTINUE_LIST_PROPERTY);
		if (l == null) {
			l = new ArrayList<JumpInsnNode>();
			node.setProperty(CONTINUE_LIST_PROPERTY, l);
		}
		return l;
	}

	/**
	 * For continue statements inside a while.
	 * Keep a list of goto instructions, for when it appears.
	 *
	 * @param node
	 *            The AST node, a continue Statement!
	 * @param list
	 *            The list of goto instructions.
	 */
	public static void setContinueList(Statement node, List<JumpInsnNode> list) {
		node.setProperty(CONTINUE_LIST_PROPERTY, list);
	}

	/**
	 * Throw a visitor Exception, and point to the node responsible.
	 *
	 * @param node
	 *            The culprit AST node!
	 * @param message
	 *            The message!
	 */
	public static void error(ASTNode node, String message) throws ASTVisitorException {
		System.out.println("Here is what exists in the accessibe symbol tables!");
		Collection<SymTableEntry> temp = getEnv(node).getSymbols();
		for (SymTableEntry ent : temp) {
			System.out.println(ent);
		}
		throw new ASTVisitorException(node.getLine() + ":" + node.getColumn() + ": " + message);
	}
}
