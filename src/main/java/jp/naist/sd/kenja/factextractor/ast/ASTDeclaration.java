package jp.naist.sd.kenja.factextractor.ast;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jp.naist.sd.kenja.factextractor.Blob;
import jp.naist.sd.kenja.factextractor.Tree;
import org.eclipse.jdt.core.dom.*;

/**
 * A class which represents declarations of Class, Enum and enum constants in Java for Historage.
 */
public class ASTDeclaration extends ASTType {
  /**
   * file name fo enum modifiers.
   */
  private static final String MODIFIERS_BLOB_NAME = "modifiers";

  /**
   * Name of root directory which store fields.
   */
  private static final String FIELD_ROOT_NAME = "[FE]";

  /**
   * Name of root directory which store constructors.
   */
  private static final String CONSTURCTOR_ROOT_NAME = "[CS]";

  /**
   * Name of root directory which store inner classes.
   */
  private static final String INNER_CLASS_ROOT_NAME = "[CN]";

  /**
   * A Blob instance corresponding to enum modifiers.
   */
  private Blob modifiers;

  /**
   * Construct ASTDeclaration from Eclipse AST AbstractTypeDeclaration class.
   *
   * @param declaration AbstractTypeDeclaration class of Eclipse AST.
   */
  public ASTDeclaration(AbstractTypeDeclaration declaration) {
    super(declaration.getName().toString());

    root.append(generateModifierBlob(declaration.getModifiers()));

    ASTField fields = new ASTField();
    Multimap<String, ASTMethod> methodMap = HashMultimap.create();

    Tree constructors = new Tree(CONSTURCTOR_ROOT_NAME);
    Tree innerClasses = null;

    for (Object obj : declaration.bodyDeclarations()) {
      if (obj instanceof FieldDeclaration) {
        FieldDeclaration fieldDeclaration = (FieldDeclaration) obj;
        fields.parseFieldDeclaration(fieldDeclaration);
      } else if (obj instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) obj;
        ASTMethod method = ASTMethod.fromMethodDeclaralation(methodDeclaration);
        if (methodDeclaration.isConstructor()) {
          constructors.append(method.getTree());
        } else {
          methodRoot.append(method.getTree());
          if (methodMap.containsKey(method.getName())) {
            int numConflicted = 0;
            for (ASTMethod astMethod : methodMap.get(method.getName())) {
              astMethod.conflict(numConflicted++);
            }
            method.conflict(numConflicted);
          }
          methodMap.put(method.getName(), method);
        }
      } else if (obj instanceof TypeDeclaration) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) obj;
        if (innerClasses == null) {
          innerClasses = new Tree(INNER_CLASS_ROOT_NAME);
          innerClasses.append(ASTClass.fromTypeDeclaration(typeDeclaration).getTree());
          root.append(innerClasses);
        }
      }
    }

    Tree fieldTree = new Tree(FIELD_ROOT_NAME);
    fieldTree.addAll(fields.getBlobs());
    root.append(fieldTree);
  }

  protected static Blob generateModifierBlob(int modifiers) {
    StringBuilder blobBody = new StringBuilder();
    if (Modifier.isPrivate(modifiers)) {
      blobBody.append("private\n");
    } else if (Modifier.isProtected(modifiers)) {
      blobBody.append("protected\n");
    } else if (Modifier.isPublic(modifiers)) {
      blobBody.append("public\n");
    } else {
      blobBody.append("package-private\n");
    }

    if (Modifier.isStatic(modifiers)) {
      blobBody.append("static\n");
    }
    if (Modifier.isAbstract(modifiers)) {
      blobBody.append("abstract\n");
    }
    if (Modifier.isFinal(modifiers)) {
      blobBody.append("final\n");
    }
    if (Modifier.isNative(modifiers)) {
      blobBody.append("native\n");
    }
    if (Modifier.isSynchronized(modifiers)) {
      blobBody.append("synchronized\n");
    }
    if (Modifier.isTransient(modifiers)) {
      blobBody.append("transient\n");
    }
    if (Modifier.isVolatile(modifiers)) {
      blobBody.append("volatile\n");
    }
    if (Modifier.isStrictfp(modifiers)) {
      blobBody.append("strictfp\n");
    }
    if (Modifier.isDefault(modifiers)) {
      blobBody.append("default\n");
    }

    return new Blob(blobBody.toString(), MODIFIERS_BLOB_NAME);
  }

  /**
   * Factory Method of ASTClass.
   *
   * @param node A TypeDeclaration of the class.
   * @return ASTClass which is corresponding to node.
   */
  public static ASTDeclaration fromTypeDeclaration(TypeDeclaration node) {
    return new ASTDeclaration(node);
  }
}
