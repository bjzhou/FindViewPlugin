package cn.bjzhou.myfirstplugin.findview;

import cn.bjzhou.myfirstplugin.utils.PsiUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.HorizontalLayout;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by zhoubinjia on 16/6/17.
 */
public class FindViewAction extends AnAction {
    private boolean mUseField = true;
    private boolean mHasParent = false;
    private Project mProject;
    private PsiElement mPsiElement;
    private PsiFile mPsiFile;
    private Editor mEditor;
    private String mFieldsStr;
    private String mMethodStr;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        mProject = getEventProject(anActionEvent);
        mPsiFile = anActionEvent.getData(PlatformDataKeys.PSI_FILE);
        mEditor = anActionEvent.getData(PlatformDataKeys.EDITOR);

        if (mProject == null || mPsiFile == null || mEditor == null) return;

        mPsiElement = mPsiFile.findElementAt(mEditor.getCaretModel().getOffset());
        try {
            if (mPsiFile.getFileType().getName().equals("JAVA")) {
                if (mPsiElement instanceof PsiIdentifierImpl) {
                    PsiIdentifierImpl field = (PsiIdentifierImpl) mPsiElement;
                    PsiFile[] files = PsiShortNamesCache.getInstance(mProject).getFilesByName(field.getText() + ".xml");

                    if (files.length > 0) {
                        showDialog(field.getText() + ".xml", files[0].getVirtualFile().contentsToByteArray(), true);
                    }
                }
            } else if (mPsiFile.getFileType().getName().equals("XML")) {
                showDialog(mPsiFile.getName(), mPsiFile.getText().getBytes(), false);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(String title, byte[] content, boolean inJava) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {

        XMLResourceExtractor resourceExtractor = XMLResourceExtractor.createResourceExtractor();
        InputStream inputStream = new ByteArrayInputStream(content);
        List<Resource> resourceList = resourceExtractor.extractResourceObjectsFromStream(inputStream);
        mFieldsStr = produceFields(resourceList);
        mMethodStr = produceMethod(resourceList);
        String displayStr = mUseField ? mFieldsStr + "\n" + mMethodStr : mMethodStr;

        DialogBuilder builder = new DialogBuilder(mProject);
        builder.setTitle(title);
        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(displayStr);
        textArea.setBorder(new LineBorder(JBColor.gray));
        JBScrollPane scrollPane = new JBScrollPane(textArea);
        JPanel jCheckBoxPanel = new JPanel(new HorizontalLayout(10));
        JCheckBox fieldCheckBox = new JCheckBox("Field");
        JCheckBox parentCheckBox = new JCheckBox("Parent");
        fieldCheckBox.setSelected(mUseField);
        parentCheckBox.setSelected(mHasParent);
        ItemListener itemListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() == fieldCheckBox) {
                    mUseField = true;
                } else {
                    mHasParent = true;
                }
            } else {
                if (e.getItem() == fieldCheckBox) {
                    mUseField = false;
                } else {
                    mHasParent = false;
                }
            }
            if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                mFieldsStr = produceFields(resourceList);
                mMethodStr = produceMethod(resourceList);
                String newDisplayStr = mUseField ? mFieldsStr + "\n" + mMethodStr : mMethodStr;
                textArea.setText(newDisplayStr);
                builder.getDialogWrapper().validate();
            }
        };
        fieldCheckBox.addItemListener(itemListener);
        parentCheckBox.addItemListener(itemListener);
        jCheckBoxPanel.add(fieldCheckBox);
        jCheckBoxPanel.add(parentCheckBox);
        jPanel.add(jCheckBoxPanel, BorderLayout.NORTH);
        jPanel.add(scrollPane, BorderLayout.CENTER);
        builder.setCenterPanel(jPanel);
        builder.addCancelAction();
        builder.addOkAction().setText("Copy");
        builder.setOkOperation(() -> {
            CopyPasteManager.getInstance().setContents(new StringSelection(textArea.getText()));
            builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        });

        if (inJava) {
            PsiMethod method = PsiUtils.findMethod(mPsiElement);
            builder.getOkAction().setText("Generate");
            builder.setOkOperation(() -> {
                new WriteCommandAction<Void>(mProject) {

                    @Override
                    protected void run(@NotNull Result<Void> result) throws Throwable {
                        if (mUseField) {
                            for (String fieldStr : mFieldsStr.split("\n")) {
                                PsiField field = PsiElementFactory.SERVICE.getInstance(mProject).createFieldFromText(fieldStr, mPsiFile);
                                method.getContainingClass().addBefore(field, method.getContainingClass().getMethods()[0]);
                            }
                        }
                        PsiMethod element = PsiElementFactory.SERVICE.getInstance(mProject).createMethodFromText(mMethodStr, mPsiFile);
                        PsiElement newEle = method.getContainingClass().addAfter(element, method);
                        mEditor.getCaretModel().moveToOffset(PsiUtils.getStartOffset(newEle.getNextSibling()));
                        mEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                    }
                }.execute();
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
            });
        }
        builder.show();
    }

    private String produceFields(List<Resource> resourceList) {
        StringBuilder builder = new StringBuilder();
        for (Resource resource : resourceList) {
            String id = generateId(resource.getResourceId());
            String type = resource.getResourceType();
            builder.append("private ").append(type).append(" ").append(id).append(";").append("\n");
        }
        return builder.toString();
    }

    private String produceMethod(List<Resource> resourceList) {
        StringBuilder builder = new StringBuilder();
        if (mHasParent) {
            builder.append("private void findViews(View parent) {").append("\n");
        } else {
            builder.append("private void findViews() {").append("\n");
        }
        for (Resource resource : resourceList) {
            String id = generateId(resource.getResourceId());
            String type = resource.getResourceType();
            String findStr = mHasParent ? "parent.findViewById" : "findViewById";
            String prefixStr = resource.isAndroidId() ? "android.R.id." : "R.id.";
            if (mUseField) {
                builder.append(String.format("    %s = (%s) %s(%s%s);", id, type, findStr, prefixStr, resource.getResourceId())).append("\n");
            } else {
                builder.append(String.format("    %s %s = (%s) %s(%s%s);", type, id, type, findStr, prefixStr, resource.getResourceId())).append("\n");
            }
        }
        builder.append("}").append("\n");
        return builder.toString();
    }

    private String generateId(String id) {
        StringBuilder builder = new StringBuilder();
        String[] splits = id.split("_");
        if (mUseField) {
            builder.append("m");
        }
        for (int i = 0; i < splits.length; i++) {
            if (i == 0 && !mUseField) {
                builder.append(splits[i].substring(0, 1).toLowerCase()).append(splits[i].substring(1));
            } else {
                builder.append(splits[i].substring(0, 1).toUpperCase()).append(splits[i].substring(1));
            }
        }
        return builder.toString();
    }
}
