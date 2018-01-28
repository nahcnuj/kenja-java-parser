package jp.naist.sd.kenja.factextractor.ast;

import jp.naist.sd.kenja.factextractor.Tree;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

public final class ASTEnum extends ASTDeclaration {
  /**
   * Name of root directory which store inner classes.
   */
  private static final String CONSTANT_ROOT_NAME = "[EC]";

  /**
   * tree of constants
   */
  private Tree constantRoot = new Tree(CONSTANT_ROOT_NAME);

  /**
   * Construct ASTEnum from Eclipse AST EnumDeclaration class.
   *
   * @param enumDec TypeDeclaration class of Eclipse AST.
   */
  private ASTEnum(EnumDeclaration enumDec) {
    super(enumDec);

    appendSuperInterfaces(enumDec.superInterfaceTypes());

    appendBodyDeclarations(enumDec.bodyDeclarations());

    root.append(constantRoot);
    for (Object obj : enumDec.enumConstants()) {
      EnumConstantDeclaration declaration = (EnumConstantDeclaration) obj;

      constantRoot.append(ASTDeclaration.fromEnumConstantDeclaration(declaration).getTree());
    }
  }

  /**
   * Factory Method of ASTEnum.
   *
   * @param node A TypeDeclaration of the enum.
   * @return ASTEnum which is corresponding to node.
   */
  public static ASTEnum fromTypeDeclaration(EnumDeclaration node) {
    return new ASTEnum(node);
  }
}