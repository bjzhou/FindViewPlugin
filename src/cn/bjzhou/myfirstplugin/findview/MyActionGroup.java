package cn.bjzhou.myfirstplugin.findview;

import com.intellij.openapi.actionSystem.DefaultActionGroup;

/**
 * Created by zhoubinjia on 16/6/17.
 */
public class MyActionGroup extends DefaultActionGroup {
    @Override
    public boolean hideIfNoVisibleChildren() {
        return true;
    }
}
