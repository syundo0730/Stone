package stone;
//import stone.ast.ASTree;

public class StoneException extends RuntimeException {
    public StoneException(String message) { super(message); }
//    public StoneException(String message, ASTree tree) {
//        super(message + " " + tree.location());
//    }
}
