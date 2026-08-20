package org.adempiere.webui;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.sys.IdGenerator;

public class SahiIdGenerator_v1 implements IdGenerator {

    private static final String ID_NUM = "Id_Num";

    @Override
    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo compInfo) {

        if (desktop == null) {
            return null;
        }

        int i = getNextIdNumber(desktop);

        if (comp != null) {
            String id = (String) comp.getAttribute("zk_component_ID");

            if (id != null && id.length() > 0) {
                if (desktop.getComponentByUuidIfAny(id) != null) {
                    desktop.setAttribute(ID_NUM, String.valueOf(i));
                    return id + "_" + i;
                }
                return id;
            }

            String prefix = (String) comp.getAttribute("zk_component_prefix");

            if (prefix == null || prefix.length() == 0) {
                prefix = "zk_comp_";
            }

            desktop.setAttribute(ID_NUM, String.valueOf(i));
            return prefix + i;
        }

        desktop.setAttribute(ID_NUM, String.valueOf(i));
        return "zk_comp_" + i;
    }

    @Override
    public String nextAnonymousComponentUuid(Component comp, ComponentInfo compInfo) {
        Desktop desktop = comp != null ? comp.getDesktop() : null;
        return nextComponentUuid(desktop, comp, compInfo);
    }

    @Override
    public String nextDesktopId(Desktop desktop) {
        if (desktop != null && desktop.getAttribute(ID_NUM) == null) {
            desktop.setAttribute(ID_NUM, "0");
        }

        return null;
    }

    @Override
    public String nextPageUuid(Page page) {
        return null;
    }

    private int getNextIdNumber(Desktop desktop) {
        Object value = desktop.getAttribute(ID_NUM);

        int i = 0;
        if (value != null) {
            try {
                i = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                i = 0;
            }
        }

        return i + 1;
    }
}