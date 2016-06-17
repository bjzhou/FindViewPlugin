package cn.bjzhou.myfirstplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.HorizontalLayout;
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

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        VirtualFile selectedFile = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);

        try {
            showDialog(project, selectedFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(Project project, VirtualFile file) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle(file.getName());
        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(produceCode(file));
        textArea.setBorder(new LineBorder(JBColor.gray));
        JBScrollPane scrollPane = new JBScrollPane(textArea);
        JPanel jCheckBoxPanel = new JPanel(new HorizontalLayout(10));
        JCheckBox field = new JCheckBox("Field");
        JCheckBox parent = new JCheckBox("Parent");
        field.setSelected(mUseField);
        parent.setSelected(mHasParent);
        ItemListener itemListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() == field) {
                    mUseField = true;
                } else {
                    mHasParent = true;
                }
            } else {
                if (e.getItem() == field) {
                    mUseField = false;
                } else {
                    mHasParent = false;
                }
            }
            if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                try {
                    textArea.setText(produceCode(file));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (XPathExpressionException e1) {
                    e1.printStackTrace();
                } catch (SAXException e1) {
                    e1.printStackTrace();
                } catch (ParserConfigurationException e1) {
                    e1.printStackTrace();
                }
                builder.getDialogWrapper().validate();
            }
        };
        field.addItemListener(itemListener);
        parent.addItemListener(itemListener);
        jCheckBoxPanel.add(field);
        jCheckBoxPanel.add(parent);
        jPanel.add(jCheckBoxPanel, BorderLayout.NORTH);
        jPanel.add(scrollPane, BorderLayout.CENTER);
        builder.setCenterPanel(jPanel);
        builder.addCancelAction();
        builder.addOkAction().setText("Copy");
        builder.setOkOperation(() -> {
            CopyPasteManager.getInstance().setContents(new StringSelection(textArea.getText()));
            builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        });
        builder.show();
    }

    private String produceCode(VirtualFile virtualFile) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        XMLResourceExtractor resourceExtractor = XMLResourceExtractor.createResourceExtractor();
        InputStream inputStream = new ByteArrayInputStream(virtualFile.contentsToByteArray());
        List<Resource> resourceList = resourceExtractor.extractResourceObjectsFromStream(inputStream);
        StringBuilder builder = new StringBuilder();
        if (mUseField) {
            for (Resource resource : resourceList) {
                String id = generateId(resource.getResourceId());
                String type = resource.getResourceType();
                builder.append("private ").append(type).append(" ").append(id).append(";").append("\n");
            }
            builder.append("\n");
        }
        if (mHasParent) {
            builder.append("private void findViews(View parent) {").append("\n");
        } else {
            builder.append("private void findViews() {").append("\n");
        }
        for (Resource resource : resourceList) {
            String id = generateId(resource.getResourceId());
            String type = resource.getResourceType();
            String findStr = mHasParent ? "parent.findViewById" : "findViewById";
            if (mUseField) {
                builder.append(String.format("    %s = (%s) %s(R.id.%s)", id, type, findStr, resource.getResourceId())).append("\n");
            } else {
                builder.append(String.format("    %s %s = (%s) %s(R.id.%s)", type, id, type, findStr, resource.getResourceId())).append("\n");
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
