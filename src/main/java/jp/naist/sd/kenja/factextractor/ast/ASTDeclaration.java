package jp.naist.sd.kenja.factextractor.ast;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jp.naist.sd.kenja.factextractor.Blob;
import jp.naist.sd.kenja.factextractor.Tree;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which represents declarations of Class, Enum and enum constants in Java for Historage.
 */
public class ASTDeclaration extends ASTType {
  /**
   * file name fo enum modifiers.
   */
  private static final String MODIFIERS_BLOB_NAME = "modifiers";

  /**
   * file name of implemented interfaces.
   */
  private static final String IMPLEMENT_BLOB_NAME = "implement";

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
   * Name of root directory which store inner enums.
   */
  private static final String INNER_ENUM_ROOT_NAME = "[EN]";

  /**
   * A Blob instance corresponding to enum modifiers.
   */
  private Blob modifiers;

  /**
   * Construct ASTDeclaration from Eclipse AST AbstractTypeDeclaration class.
   *
   * @param declaration AbstractTypeDeclaration class of Eclipse AST.
   */
  protected ASTDeclaration(AbstractTypeDeclaration declaration) {
    super(declaration.getName().toString());

    root.append(generateModifierBlob(declaration.modifiers()));

    appendBodyDeclarations(declaration.bodyDeclarations());
  }

  /**
   * Construct ASTDeclaration from Eclipse AST EnumConstantDeclaration class.
   *
   * @param declaration EnumConstantDeclaration class of Eclipse AST.
   */
  protected ASTDeclaration(EnumConstantDeclaration declaration) {
    super(declaration.getName().getIdentifier());

    root.append(generateModifierBlob(declaration.modifiers()));

    AnonymousClassDeclaration anonymousClassDeclaration = declaration.getAnonymousClassDeclaration();
    if (anonymousClassDeclaration != null) {
      appendBodyDeclarations(anonymousClassDeclaration.bodyDeclarations());
    }
  }

  private static Blob generateModifierBlob(List modifiers) {
    StringBuilder blobBody = new StringBuilder();
    for (Object obj : modifiers) {
      if (!(obj instanceof IExtendedModifier)) {
        throw new IllegalArgumentException();
      }
      IExtendedModifier exmodifier = (IExtendedModifier) obj;
      if (exmodifier.isModifier()) {
        Modifier modifier = (Modifier) exmodifier;
        blobBody.append(modifier.getKeyword().toString());
      } else if (exmodifier.isAnnotation()) {
        Annotation annotation = (Annotation) exmodifier;
        blobBody.append("@")
            .append(annotation.getTypeName());
        if (annotation.isSingleMemberAnnotation()) {
          blobBody.append("(")
              .append(getStringOfConstantExpression(((SingleMemberAnnotation) annotation).getValue()))
              .append(")");
        } else if (annotation.isNormalAnnotation()) {
          List<String> pairs = new ArrayList<>();
          for (Object o : ((NormalAnnotation) annotation).values()) {
            MemberValuePair pair = (MemberValuePair) o;
            blobBody.append(pair.getName().getIdentifier())
                .append("=")
                .append(getStringOfConstantExpression(pair.getValue()));
          }
          blobBody.append("(")
              .append(String.join(",", pairs))
              .append(")");
        }
      }
      blobBody.append("\n");
    }
    return new Blob(blobBody.toString(), MODIFIERS_BLOB_NAME);
  }

  /**
   * Return string of expression if the expression is constant in compile-time, or string for debugging otherwise.
   *
   * @param expression expression
   * @return string of constant expression or debugging string of expression otherwise
   */
  private static String getStringOfConstantExpression(Expression expression) {
    Object value = expression.resolveConstantExpressionValue();
    return value != null ? value.toString() : expression.toString();
  }

  /**
   * Factory Method of ASTDeclaration.
   *
   * @param node A EnumConstantDeclaration of the class.
   * @return ASTDeclaration which is corresponding to node.
   */
  public static ASTDeclaration fromEnumConstantDeclaration(EnumConstantDeclaration node) {
    return new ASTDeclaration(node);
  }

  /**
   * Append body declarations to tree.
   */
  protected void appendBodyDeclarations(List declarations) {
    ASTField fields = new ASTField();
    Multimap<String, ASTMethod> methodMap = HashMultimap.create();

    Tree constructors = new Tree(CONSTURCTOR_ROOT_NAME);
    Tree fieldTree = null;
    Tree innerClasses = null;
    Tree innerEnums = null;

    for (Object obj : declarations) {
      System.setOut(System.out);
      System.out.printf("%s\n", obj.getClass().getName());
      if (obj instanceof FieldDeclaration) {
        if (fieldTree == null) {
          fieldTree = new Tree(FIELD_ROOT_NAME);
        }
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
          root.append(innerClasses);
        }
        innerClasses.append(ASTClass.fromTypeDeclaration(typeDeclaration).getTree());
      } else if (obj instanceof EnumDeclaration) {
        EnumDeclaration enumDeclaration = (EnumDeclaration) obj;
        if (innerEnums == null) {
          innerEnums = new Tree(INNER_ENUM_ROOT_NAME);
          root.append(innerEnums);
        }
        innerEnums.append(ASTEnum.fromTypeDeclaration(enumDeclaration).getTree());
      }
    }
    if (fieldTree != null) {
      fieldTree.addAll(fields.getBlobs());
      root.append(fieldTree);
    }
  }

  /**
   * Append super interfaces to tree if implements some ones.
   *
   * @param interfaces list of super interfaces
   */
  protected void appendSuperInterfaces(List interfaces) {
    if (!interfaces.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Object obj : interfaces) {
        Type interfaceType = (Type) obj;
        sb.append(interfaceType.toString())
            .append("\n");
      }
      root.append(new Blob(sb.toString(), IMPLEMENT_BLOB_NAME));
    }
  }
}
