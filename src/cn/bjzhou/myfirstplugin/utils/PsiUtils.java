package cn.bjzhou.myfirstplugin.utils;

import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created by zhoubinjia on 16/6/22.
 */
public class PsiUtils {

    public static PsiMethod findMethod(PsiElement element) {
        PsiMethod method = (element instanceof PsiMethod) ? (PsiMethod) element :
                PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method != null && method.getContainingClass() instanceof PsiAnonymousClass) {
            return findMethod(method.getParent());
        }
        return method;
    }

    public static int getStartOffset(PsiElement element) {
        if (element != null && element.getStartOffsetInParent() != 0) {
            return element.getStartOffsetInParent() + getStartOffset(element.getParent());
        } else {
            return 0;
        }
    }
}
