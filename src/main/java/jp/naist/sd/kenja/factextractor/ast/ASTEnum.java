package jp.naist.sd.kenja.factextractor.ast;

import org.eclipse.jdt.core.dom.EnumDeclaration;

public final class ASTEnum extends ASTDeclaration {
  /**
   * Construct ASTEnum from Eclipse AST EnumDeclaration class.
   *
   * @param enumDec TypeDeclaration class of Eclipse AST.
   */
  private ASTEnum(EnumDeclaration enumDec) {
    super(enumDec);

    appendSuperInterfaces(enumDec.superInterfaceTypes());
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