/******************************************************************************
 * Product: Posterita Ajax UI 												  *
 * Copyright (C) 2007 Posterita Ltd.  All Rights Reserved.                    *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Posterita Ltd., 3, Draper Avenue, Quatre Bornes, Mauritius                 *
 * or via info@posterita.org or http://www.posterita.org/                     *
 *****************************************************************************/

package org.adempiere.webui.panel;

import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.window.AboutWindow;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.West;
import org.zkoss.zul.Image;
import org.zkoss.zul.Vbox;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @author  <a href="mailto:hengsin@gmail.com">Low Heng Sin</a>
 * @date    Mar 2, 2007
 * @date    July 7, 2007
 * @version $Revision: 0.20 $
 */

public class HeaderPanel extends Panel implements EventListener
{
	private static final long serialVersionUID = -2351317624519209484L;

	private static final String HEADER_HEIGHT = "50px";

	private Image image = new Image();

    public HeaderPanel()
    {
        super();
        init();
    }

    private void init()
    {
    	LayoutUtils.addSclass("desktop-header", this);
    	setHeight(HEADER_HEIGHT);
    	setStyle("height:" + HEADER_HEIGHT + "; min-height:" + HEADER_HEIGHT + "; overflow:visible; padding:0; margin:0;");

    	UserPanel userPanel = new UserPanel();

    	image.setSrc(ThemeManager.getSmallLogo());
    	image.addEventListener(Events.ON_CLICK, this);
    	image.setStyle("cursor:pointer; max-height:52px; max-width:190px; width:auto; object-fit:contain;");

    	Borderlayout layout = new Borderlayout();
    	LayoutUtils.addSclass("desktop-header", layout);
    	layout.setParent(this);
    	layout.setWidth("100%");
    	layout.setHeight(HEADER_HEIGHT);
    	layout.setStyle("height:" + HEADER_HEIGHT + "; min-height:" + HEADER_HEIGHT + "; overflow:visible; border:none; padding:0; margin:0;");

    	West west = new West();
    	west.setParent(layout);
    	west.setWidth("230px");
    	west.setBorder("none");
    	west.setSplittable(false);
    	west.setCollapsible(false);
    	west.setStyle("background-color:transparent; border:none; padding:0; margin:0; overflow:hidden;");

    	Vbox vb = new Vbox();
    	vb.setParent(west);
    	vb.setWidth("100%");
    	vb.setHeight(HEADER_HEIGHT);
    	vb.setPack("center");
    	vb.setAlign("center");
    	vb.setStyle("height:" + HEADER_HEIGHT + "; padding:0 10px; box-sizing:border-box; overflow:hidden;");

    	image.setParent(vb);

    	LayoutUtils.addSclass("desktop-header-left", west);

    	Center center = new Center();
    	center.setParent(layout);
    	center.setBorder("none");
    	center.setStyle("background-color:transparent; border:none; padding:0; margin:0; overflow:visible;");

    	userPanel.setParent(center);
    	userPanel.setWidth("100%");
    	userPanel.setHeight(HEADER_HEIGHT);
    	userPanel.setStyle("height:" + HEADER_HEIGHT + "; min-height:" + HEADER_HEIGHT + "; position:relative; overflow:visible;");

    	LayoutUtils.addSclass("desktop-header-right", center);
    }

	public void onEvent(Event event) throws Exception {
		if (Events.ON_CLICK.equals(event.getName())) {
			if(event.getTarget() == image)
			{
				AboutWindow w = new AboutWindow();
				w.setPage(this.getPage());
				w.doModal();
			}
		}

	}
}
