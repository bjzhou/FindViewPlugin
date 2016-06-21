package cn.bjzhou.myfirstplugin.mock;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;

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

        String content = "";

        if (PSI_ELEMENT instanceof PsiFieldImpl) {
            PsiFieldImpl field = (PsiFieldImpl) PSI_ELEMENT;
            PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(field.getName() + ".xml");

            content = field.getName() + "\n" +
                    field.getInitializer().getText() + "\n" +
                    field.getType().getPresentableText() + "\n" +
                    field.getContainingClass().getName() + "\n" +
                    (files.length > 0 ? files[0].getVirtualFile().getPath() : "null");
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
