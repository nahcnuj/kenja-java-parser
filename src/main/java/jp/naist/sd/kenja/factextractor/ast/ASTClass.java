package jp.naist.sd.kenja.factextractor.ast;

import jp.naist.sd.kenja.factextractor.Blob;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

import java.util.List;

/**
 * A class which represents Class of Java for Historage.
 *
 * @author Kenji Fujiwara
 */
public final class ASTClass extends ASTDeclaration {
  /**
   * file name of extended class.
   */
  private static final String EXTEND_BLOB_NAME = "extend";

  /**
   * file name of type parameters.
   */
  private static final String TYPE_PARAMETERS_BLOB_NAME = "typeparameters";

  /**
   * Construct ASTClass from Eclipse AST TypeDeclaration class.
   *
   * @param declaration TypeDeclaration class of Eclipse AST.
   */
  private ASTClass(TypeDeclaration declaration) {
    super(declaration);

    List typeParameters = declaration.typeParameters();
    if (!typeParameters.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Object obj : typeParameters) {
        TypeParameter param = (TypeParameter) obj;
        // todo modifier
        // param.modifiers()
        sb.append(param.getName().getIdentifier())
            .append("\n");
        // todo type bounds
        // param.typeBounds()
      }
      root.append(new Blob(sb.toString(), TYPE_PARAMETERS_BLOB_NAME));
    }

    Type superclassType = declaration.getSuperclassType();
    if (superclassType != null) {
      root.append(new Blob(superclassType.toString() + "\n", EXTEND_BLOB_NAME));
    }

    appendSuperInterfaces(declaration.superInterfaceTypes());
  }

  /**
   * Factory Method of ASTClass.
   *
   * @param node A TypeDeclaration of the class.
   * @return ASTClass which is corresponding to node.
   */
  public static ASTClass fromTypeDeclaration(TypeDeclaration node) {
    return new ASTClass(node);
  }
}
