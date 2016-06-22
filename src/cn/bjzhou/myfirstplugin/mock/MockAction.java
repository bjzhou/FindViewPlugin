package cn.bjzhou.myfirstplugin.mock;

import cn.bjzhou.myfirstplugin.utils.PsiUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by zhoubinjia on 16/6/20.
 */
public class MockAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        VirtualFile file = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        String FILE_TEXT = anActionEvent.getData(PlatformDataKeys.FILE_TEXT);
        PsiElement PSI_ELEMENT = anActionEvent.getData(PlatformDataKeys.PSI_ELEMENT);
        PsiFile PSI_FILE = anActionEvent.getData(PlatformDataKeys.PSI_FILE);
        Editor EDITOR = anActionEvent.getData(PlatformDataKeys.EDITOR);

        String content = "";

//        if (PSI_ELEMENT instanceof PsiFieldImpl) {
//            PsiFieldImpl field = (PsiFieldImpl) PSI_ELEMENT;
//            PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(field.getName() + ".xml");
//
//            content = field.getName() + "\n" +
//                    field.getInitializer().getText() + "\n" +
//                    field.getType().getPresentableText() + "\n" +
//                    field.getContainingClass().getName() + "\n" +
//                    PSI_ELEMENT.getContext().getText() + "\n" +
//                    (files.length > 0 ? files[0].getVirtualFile().getPath() : "null");
//        }

        if (PSI_FILE == null || EDITOR == null) return;

        PsiElement pe = PSI_FILE.findElementAt(EDITOR.getCaretModel().getOffset());
        if (pe != null) {
            try {
                String result = new WriteCommandAction<String>(project, PSI_FILE) {
                    @Override
                    protected void run(@NotNull Result<String> result) throws Throwable {
                        PsiMethod method = PsiUtils.findMethod(pe);
                        PsiMethod element = PsiElementFactory.SERVICE.getInstance(project).createMethodFromText("private void test() {\n    finish();\n}", PSI_FILE);
                        PsiElement newEle = method.getContainingClass().addAfter(element, method);
                        PsiField field = PsiElementFactory.SERVICE.getInstance(project).createFieldFromText("private String testStr;", PSI_FILE);
                        method.getContainingClass().addBefore(field, method.getContainingClass().getMethods()[0]);
                        int newLineNumber = EDITOR.getDocument().getLineNumber(newEle.getNextSibling().getStartOffsetInParent() + newEle.getContext().getStartOffsetInParent());
                        int lineNumber = EDITOR.getDocument().getLineNumber(EDITOR.getCaretModel().getOffset());
                        EDITOR.getCaretModel().moveToOffset(newEle.getNextSibling().getStartOffsetInParent() + newEle.getContext().getStartOffsetInParent());
                        EDITOR.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                        result.setResult("line number : " + lineNumber + " new line number : " + newLineNumber);
                    }
                }.execute().getResultObject();
                content += "\n" + result;
            } catch (Throwable e) {
                content = "throwable : " + e.toString();
            }
            content += "\n" + EDITOR.getCaretModel().getOffset();
        }

        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle(file.getName());
        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(content);
        textArea.setBorder(new LineBorder(JBColor.gray));
        JBScrollPane scrollPane = new JBScrollPane(textArea);
        jPanel.add(scrollPane, BorderLayout.CENTER);
        builder.setCenterPanel(jPanel);
        builder.show();
    }
}
