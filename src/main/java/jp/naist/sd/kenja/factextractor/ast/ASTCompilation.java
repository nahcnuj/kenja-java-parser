package jp.naist.sd.kenja.factextractor.ast;

import jp.naist.sd.kenja.factextractor.Blob;
import jp.naist.sd.kenja.factextractor.Tree;
import jp.naist.sd.kenja.factextractor.Treeable;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A class which represents Compilation of Java for Historage.
 *
 * @author Kenji Fujiwara
 */
public class ASTCompilation implements Treeable {

  /**
   * Name of root directory which store classes.
   */
  private static final String CLASS_ROOT_NAME = "[CN]";
  /**
   * Name of root directory which store interfaces.
   */
  private static final String INTERFACE_ROOT_NAME = "[IN]";
  /**
   * Name of root directory which store enums.
   */
  private static final String ENUM_ROOT_NAME = "[EN]";
  /**
   * root Tree of Java file.
   */
  private Tree root;
  /**
   * package information of a Java file.
   */
  private ASTPackage pack;
  /**
   * root Tree of classes.
   */
  private Tree classRoot;
  /**
   * root Tree of classes.
   */
  private Tree enumRoot;

  /**
   * List of ASTClass.
   */
  private List<ASTClass> classes = new LinkedList<>();

  /**
   * root Tree of interfaces.
   */
  private Tree interfaceRoot;

  /**
   * Default constructor for ASTCompilation.
   */
  protected ASTCompilation() {

  }

  /**
   * Create ASTCompilation from CompilationUnit of Eclipse AST and root Tree.
   *
   * @param unit CompilationUnit of Eclipse AST
   * @param root root Tree
   */
  public ASTCompilation(CompilationUnit unit, Tree root) {
    this.root = root;
    if (unit.getPackage() != null) {
      pack = ASTPackage.fromPackageDeclaration(unit.getPackage());
      for (Blob blob : pack.getBlobs()) {
        root.append(blob);
      }
    }

    addTypes(unit);
  }

  /**
   * Factory method for creating a ASTCompilation instance.
   *
   * @param unit CompilationUnit of Eclipse AST.
   * @return ASTCompilation instance which is corresponding to unit.
   */
  public static ASTCompilation fromCompilation(CompilationUnit unit) {
    return new ASTCompilation(unit, new Tree(""));
  }

  /**
   * ?????
   *
   * @param baseDir ????
   * @return ?????
   */
  public List<String> getChangedFileList(File baseDir) {
    return root.getObjectsPath("");
  }

  /**
   * returns root of classes.
   *
   * @return root of classes
   */
  private Tree getClassRoot() {
    if (classRoot == null) {
      classRoot = new Tree(CLASS_ROOT_NAME);
      // getTypeRoot().append(classRoot);
      root.append(classRoot);
    }

    return classRoot;

  }

  /**
   * returns root of enums.
   *
   * @return root of enums
   */
  private Tree getEnumRoot() {
    if (enumRoot == null) {
      enumRoot = new Tree(ENUM_ROOT_NAME);
      root.append(enumRoot);
    }

    return enumRoot;
  }

  /**
   * returns root of interfaces.
   *
   * @return root of interfaces
   */
  private Tree getInterfaceRoot() {
    if (interfaceRoot == null) {
      interfaceRoot = new Tree(INTERFACE_ROOT_NAME);
      root.append(interfaceRoot);
    }

    return interfaceRoot;
  }

  /**
   * add classes, interfaces and enums to compilation unit.
   *
   * @param unit compilation unit of Eclipse AST
   */
  public void addTypes(CompilationUnit unit) {
    for (Object obj : unit.types()) {
      AbstractTypeDeclaration abstTypeDec = (AbstractTypeDeclaration) obj;
      if (abstTypeDec instanceof EnumDeclaration) {
        EnumDeclaration enumDec = (EnumDeclaration) abstTypeDec;
        ASTEnum astEnum = ASTEnum.fromTypeDeclaration(enumDec);
        getEnumRoot().append(astEnum.getTree());
      } else {
        TypeDeclaration typeDec = (TypeDeclaration) abstTypeDec;
        ASTClass astClass = ASTClass.fromTypeDeclaration(typeDec);
        if (typeDec.isInterface()) {
          getInterfaceRoot().append(astClass.getTree());
        } else {
          getClassRoot().append(astClass.getTree());
        }
      }
    }
  }

  @Override
  public Tree getTree() {
    return root;
  }
}
