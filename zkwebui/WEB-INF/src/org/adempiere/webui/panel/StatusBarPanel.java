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

import java.net.URI;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.window.WRecordInfo;
import org.compiere.apps.IStatusBar;
import org.compiere.model.DataStatusEvent;
import org.compiere.model.MRole;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.North;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.South;

/**
 * This class is based on org.compiere.apps.StatusBar written by Jorg Janke.
 * @author Jorg Janke
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date    Mar 12, 2007
 * @version $Revision: 0.10 $
 */
public class StatusBarPanel extends Panel implements EventListener, IStatusBar
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3262889055635240201L;
	
	private static final String POPUP_INFO_BACKGROUND_STYLE = "background-color: #262626; -moz-border-radius: 3px; -webkit-border-radius: 3px; border: 1px solid #262626; border-radius: 3px; ";
	private static final String POPUP_ERROR_BACKGROUND_STYLE = "background-color: #8B0000; -moz-border-radius: 3px; -webkit-border-radius: 3px; border: 1px solid #8B0000; border-radius: 3px; ";
	private static final String POPUP_POSITION_STYLE = "position: absolute; z-index: 99; display: block; visibility: visible;";
	private static final String POPUP_TEXT_STYLE = "color: white; background-color: transparent; font-size: 14px; font-weight:bold; position: relative; -moz-box-shadow: 0px 0px 0px #000;-webkit-box-shadow: 0px 0px 0px #000;box-shadow: 0px 0px 0px #000; padding: 5px; width: 590px; min-height: 20px;";

	private static final String SHADOW_STYLE = "-moz-box-shadow: 2px 2px 2px #888; -webkit-box-shadow: 2px 2px 2px #888; box-shadow: 2px 2px 2px #888;";

	private Label statusDB;
    private Label infoLine;
    private Label statusLine;

	private DataStatusEvent m_dse;

	private String m_text;

	private Div east;

	private Div west;

	private Div popup;

	//private Div popupContent;
	//private String popupStyle;
	private boolean embedded;
	
	private Borderlayout layout = new Borderlayout();
	
	private Image image = new Image();

	private North north;
	
	private South south;

	public StatusBarPanel()
	{
		this(false);
	}

	/**
	 * @param embedded
	 */
    public StatusBarPanel(boolean embedded)
    {
        super();
        this.embedded = embedded;
        init();
    }

    private void init()
    {
        statusDB = new Label("  ");
        statusLine = new Label();

        Div row = new Div();
        row.setWidth("100%");
        row.setHeight("100%");
        LayoutUtils.addSclass("statusbar-row", row);

        URI uri = AEnv.getImage("errormsg.png");
        image.setSrc(uri.toString());
        image.setVisible(false);
        image.setWidth("16px");
        image.setHeight("16px");
        LayoutUtils.addSclass("status-image", image);
        row.appendChild(image);

        west = new Div();
        LayoutUtils.addSclass("status-left", west);
        LayoutUtils.addSclass("status-line-container", west);

        LayoutUtils.addSclass("status-line", statusLine);
        west.appendChild(statusLine);
        row.appendChild(west);

        east = new Div();
        LayoutUtils.addSclass("status-detail", east);
        LayoutUtils.addSclass("status-detail-container", east);

        if (!embedded)
        {
            infoLine = new Label();
            infoLine.setVisible(false);
            LayoutUtils.addSclass("status-info", infoLine);
            east.appendChild(infoLine);
        }

        LayoutUtils.addSclass("status-db", statusDB);
        east.appendChild(statusDB);

        row.appendChild(east);

        this.appendChild(row);

        statusDB.addEventListener(Events.ON_CLICK, this);
    }

    /**
     * @param text
     */
    public void setStatusDB (String text)
    {
        setStatusDB(text, null);
    }

    /**
     * @param text
     * @param dse
     */
    public void setStatusDB (String text, DataStatusEvent dse)
    {
        if (text == null || text.length() == 0)
        {
            statusDB.setValue("");
        }
        else
        {
            StringBuffer sb = new StringBuffer (" ");
            sb.append(text).append(" ");
            statusDB.setValue(sb.toString());
        }

        m_text = text;
        m_dse = dse;
    }

    /**
     * @param text
     */
    public void setStatusLine (String text)
    {
        setStatusLine(text, false);
    }

    /**
     * @param text
     * @param error
     */
    public void setStatusLine (String text, boolean error)
    {
    	setStatusLine(text, error, error);
    }

    /**
     * @param text
     * @param error
     * @param showPopup ignore for embedded
     */
    public void setStatusLine(String text, boolean error, boolean showPopup)
    {
        String value = text != null ? text : "";
        statusLine.setText(value);
        statusLine.setTooltiptext(value);

        if (error)
        {
            setMessageSclass("message-error");
            statusLine.setSclass("status-line message-error-text");
            image.setVisible(true);

            this.setHeight("50px");

            if (south != null)
                south.setHeight("50px");
        }
        else
        {
            setMessageSclass("message-info");
            statusLine.setSclass("status-line message-info-text");
            image.setVisible(false);

            this.setHeight("22px");

            if (south != null)
                south.setHeight("22px");
        }

        this.invalidate();

        if (south != null)
            south.invalidate();
    }
    
    public void setSouth(South s) {
        this.south = s;
    }

    private void setMessageSclass(String messageClass)
    {
    	String currentSclass = getSclass();
    	if (currentSclass != null && currentSclass.contains("adwindow-status"))
    		setSclass("adwindow-status " + messageClass);
    	else
    		setSclass(messageClass);
    }

    /*
	private void createPopup() {
		popupContent = new Div();

		popup = new Div();
        popup.setWidth("600px");
        popup.appendChild(popupContent);
        popup.addEventListener(Events.ON_CLICK, this);
        popup.setPage(SessionManager.getAppDesktop().getComponent().getPage());
        popup.setStyle("position: absolute; display: none");
	}*/

	private void showPopup() {
		//popup.setVisible(true);
		//popup.setStyle(popupStyle);
		//popupContent.setVisible(true);
		/*
		String script = "var d = $e('" + popup.getUuid() + "');";
		script += "d.style.display='block';d.style.visibility='hidden';";
		script += "var dhs = document.defaultView.getComputedStyle(d, null).getPropertyValue('height');";
		script += "var dh = parseInt(dhs, 10);";
		script += "var r = $e('" + getRoot().getUuid() + "');";
		script += "var rhs = document.defaultView.getComputedStyle(r, null).getPropertyValue('height');";
		script += "var rh = parseInt(rhs, 10);";
		script += "var p = Position.cumulativeOffset(r);";
		//script += "d.style.top=(rh-dh)+'px';";
		script += "d.style.top=(rh/2-dh/2)+'px';";
		//script += "d.style.left=(p[0]+1)+'px';";
		script += "d.style.left=(lh/2-dw/2)+'px';";
		script += "d.style.visibility='visible';";

		AuScript aus = new AuScript(popup, script);
		Clients.response(aus);
		*/
	}

    /**
     * Add Component to East of StatusBar
     *
     * @param component
     *            component
     */
    public final void addStatusComponent(final Component component)
    {
        east.appendChild(component);
    } // addStatusComponent

    /**
	 *	Set Info Line
	 *  @param text text
	 */
    public void setInfo(String text)
    {
        if (!embedded)
        {
            String value = text != null ? text.trim() : "";
            infoLine.setValue(value);
            infoLine.setTooltiptext(value);
            infoLine.setVisible(value.length() > 0);
        }
    }	//	setInfo

	public void onEvent(Event event) throws Exception {
		if (Events.ON_CLICK.equals(event.getName()) && event.getTarget() == statusDB)
		{
			if (m_dse == null
				|| m_dse.CreatedBy == null
				|| !MRole.getDefault().isShowPreference())
				return;

			String title = Msg.getMsg(Env.getCtx(), "Who") + m_text;
			new WRecordInfo (title, m_dse);
		}
		else if (Events.ON_CLICK.equals(event.getName()) && event.getTarget() == popup)
		{
			popup.setVisible(false);
		}
	}

	@Override
	public void onPageDetached(Page page) {
		super.onPageDetached(page);
		if (popup != null)
			popup.detach();
	}

	/**
	 * @param visible
	 */
	public void setEastVisibility(boolean visible) {
		east.setVisible(visible);
	}

	public void setNorth(North n) {
		this.north = n;
	}

}
