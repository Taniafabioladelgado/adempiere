/******************************************************************************
 * Copyright (C) 2008 Low Heng Sin                                            *
 * Copyright (C) 2008 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.adempiere.webui.desktop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.model.MBrowse;
import org.adempiere.webui.apps.ProcessDialog;
import org.adempiere.webui.apps.wf.WFPanel;
import org.adempiere.webui.component.DesktopTabpanel;
import org.adempiere.webui.component.Tab;
import org.adempiere.webui.component.Tabbox;
import org.adempiere.webui.component.Tabpanel;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.part.WindowContainer;
import org.adempiere.webui.window.ADWindow;
import org.adempiere.webui.window.WTask;
import org.compiere.model.MQuery;
import org.compiere.model.MTask;
import org.compiere.util.Env;
import org.compiere.util.WebDoc;
import org.compiere.wf.MWorkflow;
import org.eevolution.form.WBrowser;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Tabpanels;

import javax.servlet.http.HttpSession;

/**
 * A Tabbed MDI implementation
 * @author hengsin
 * @author victor.perez@e-evoluton.com, www.e-evolution.com 
 * 	<li>FR [ 3426137 ] Smart Browser
 *  https://sourceforge.net/tracker/?func=detail&aid=3426137&group_id=176962&atid=879335
 *
 */
public abstract class TabbedDesktop extends AbstractDesktop {

	private static final String SAVED_TABS_ATTRIBUTE = TabbedDesktop.class.getName() + ".savedTabs";
	private static final String SAVED_TAB_ATTRIBUTE = TabbedDesktop.class.getName() + ".savedTab";
	private static final String TAB_TYPE_WINDOW = "WINDOW";
	private static final String TAB_TYPE_FORM = "FORM";
	private static final String TAB_TYPE_BROWSE = "BROWSE";
	private static final String TAB_TYPE_WORKFLOW = "WORKFLOW";
	private static final String TAB_TYPE_PROCESS = "PROCESS";

	protected WindowContainer windowContainer;
	private boolean restoringSavedTabs = false;

	public TabbedDesktop() {
		super();
		windowContainer = new WindowContainer();
	}

	/**
     *
     * @param processId
     * @param soTrx
     * @return ProcessDialog
     */
	public ProcessDialog openProcessDialog(int processId, boolean soTrx) {
		ProcessDialog pd = new ProcessDialog (processId, soTrx);
		if (pd.isValid()) {
			DesktopTabpanel tabPanel = new DesktopTabpanel();
			pd.setParent(tabPanel);
			String title = pd.getTitle();
			pd.setTitle(null);
			preOpenNewTab();
			windowContainer.addWindow(tabPanel, title, true);
			rememberOpenedTab(new SavedTabState(TAB_TYPE_PROCESS, processId, soTrx));
			pd.afterInit();
		}
		return pd;
	}

    /**
     *
     * @param formId
     * @return ADWindow
     */
	public ADForm openForm(int formId) {
		ADForm form = ADForm.openForm(formId);

		DesktopTabpanel tabPanel = new DesktopTabpanel();
		form.setParent(tabPanel);
		//do not show window title when open as tab
		form.setTitle(null);
		form.setAttribute(WINDOWNO_ATTRIBUTE, form.getWindowNo());
		preOpenNewTab();
		windowContainer.addWindow(tabPanel, form.getFormName(), true);
		rememberOpenedTab(new SavedTabState(TAB_TYPE_FORM, formId, false));

		return form;
	}
	
	public CustomForm openBrowse(int browseId, Boolean isSOTrx)
	{
		MBrowse browse = new MBrowse(Env.getCtx() ,browseId, null);
		CustomForm ff =  WBrowser.openBrowse(0 , browseId, "", isSOTrx );
		DesktopTabpanel tabPanel = new DesktopTabpanel();
        ff.setParent(tabPanel);
        preOpenNewTab();
        windowContainer.addWindow(tabPanel, browse.getTitle(), true);
        rememberOpenedTab(new SavedTabState(TAB_TYPE_BROWSE, browseId, Boolean.TRUE.equals(isSOTrx)));
		return  ff;
	}

	/**
	 *
	 * @param workflow_ID
	 */
	public void openWorkflow(int workflow_ID) {
		WFPanel p = new WFPanel();
		p.load(workflow_ID);

		DesktopTabpanel tabPanel = new DesktopTabpanel();
		p.setParent(tabPanel);
		preOpenNewTab();
		windowContainer.addWindow(tabPanel, p.getWorkflow().get_Translation(MWorkflow.COLUMNNAME_Name), true);
		rememberOpenedTab(new SavedTabState(TAB_TYPE_WORKFLOW, workflow_ID, false));
	}

	/**
	 *
	 * @param windowId
	 * @return ADWindow
	 */
	public ADWindow openWindow(int windowId) {
		ADWindow adWindow = new ADWindow(Env.getCtx(), windowId);

		DesktopTabpanel tabPanel = new DesktopTabpanel();
		if (adWindow.createPart(tabPanel) != null) {
			preOpenNewTab();
			windowContainer.addWindow(tabPanel, adWindow.getTitle(), true);
			rememberOpenedTab(new SavedTabState(TAB_TYPE_WINDOW, windowId, false));
			return adWindow;
		} else {
			//user cancel
			return null;
		}
	}

	/**
	 *
	 * @param windowId
     * @param query
	 * @return ADWindow
	 */
	public ADWindow openWindow(int windowId, MQuery query) {
    	ADWindow adWindow = new ADWindow(Env.getCtx(), windowId, query);

		DesktopTabpanel tabPanel = new DesktopTabpanel();
		if (adWindow.createPart(tabPanel) != null) {
			preOpenNewTab();
			windowContainer.addWindow(tabPanel, adWindow.getTitle(), true);
			rememberOpenedTab(new SavedTabState(TAB_TYPE_WINDOW, windowId, false));
			return adWindow;
		} else {
			//user cancel
			return null;
		}
	}

	/**
     *
     * @param taskId
     */
	public void openTask(int taskId) {
		MTask task = new MTask(Env.getCtx(), taskId, null);
		new WTask(task.getName(), task);
	}

	/**
	 * @param url
	 */
	public void showURL(String url, boolean closeable)
    {
    	showURL(url, url, closeable);
    }

	/**
	 *
	 * @param url
	 * @param title
	 * @param closeable
	 */
    public void showURL(String url, String title, boolean closeable)
    {
    	Iframe iframe = new Iframe(url);
    	addWin(iframe, title, closeable);
    }

    /**
     * @param webDoc
     * @param title
     * @param closeable
     */
    public void showURL(WebDoc webDoc, String title, boolean closeable)
    {
    	Iframe iframe = new Iframe();

    	AMedia media = new AMedia(title, "html", "text/html", webDoc.toString().getBytes());
    	iframe.setContent(media);

    	addWin(iframe, title, closeable);
    }

    /**
     *
     * @param fr
     * @param title
     * @param closeable
     */
    private void addWin(Iframe fr, String title, boolean closeable)
    {
    	fr.setHflex("1");
        fr.setVflex("1");
        fr.setStyle("padding: 0; margin: 0; border: none");
        Window window = new Window();
        window.setHflex("1");
        window.setVflex("1");
        window.setStyle("padding: 0; margin: 0; border: none");
        window.appendChild(fr);

        Tabpanel tabPanel = new Tabpanel();
    	window.setParent(tabPanel);
    	preOpenNewTab();
    	windowContainer.addWindow(tabPanel, title, closeable);
    }

    /**
     * @param AD_Window_ID
     * @param query
     */
    public void showZoomWindow(int AD_Window_ID, MQuery query)
    {
    	ADWindow wnd = new ADWindow(Env.getCtx(), AD_Window_ID, query);

    	DesktopTabpanel tabPanel = new DesktopTabpanel();
    	if (wnd.createPart(tabPanel) != null)
    	{
    		preOpenNewTab();
    		windowContainer.insertAfter(windowContainer.getSelectedTab(), tabPanel, wnd.getTitle(), true, true);
    		rememberOpenedTab(new SavedTabState(TAB_TYPE_WINDOW, AD_Window_ID, false));
    	}
	}

    /**
     * @param AD_Window_ID
     * @param query
     * @deprecated
     */
    public void showWindow(int AD_Window_ID, MQuery query)
    {
    	openWindow(AD_Window_ID, query);
	}

	/**
	 *
	 * @param window
	 */
	protected void showEmbedded(Window window)
   	{
		Tabpanel tabPanel = new Tabpanel();
    	window.setParent(tabPanel);
    	String title = window.getTitle();
    	window.setTitle(null);
    	preOpenNewTab();
    	if (Window.INSERT_NEXT.equals(window.getAttribute(Window.INSERT_POSITION_KEY)))
    		windowContainer.insertAfter(windowContainer.getSelectedTab(), tabPanel, title, true, true);
    	else
    		windowContainer.addWindow(tabPanel, title, true);
   	}

	/**
	 * Close active tab
	 * @return boolean
	 */
	public boolean closeActiveWindow()
	{
		if (windowContainer.getSelectedTab() != null)
		{
			Tabpanel panel = (Tabpanel) windowContainer.getSelectedTab().getLinkedPanel();
			if(panel != null) {
				Component component = panel.getFirstChild();
				Object att = component.getAttribute(WINDOWNO_ATTRIBUTE);
	
				if ( windowContainer.closeActiveWindow() )
				{
					if (att != null && (att instanceof Integer))
					{
						unregisterWindow((Integer) att);
					}
					persistOpenTabsFromUI();
					return true;
				}
				else
				{
					return false;
				} 
			}
		}
		return false;
	}

	/**
	 * @return Component
	 */
	public Component getActiveWindow()
	{
		return windowContainer.getSelectedTab().getLinkedPanel().getFirstChild();
	}

	/**
	 *
	 * @param windowNo
	 * @return boolean
	 */
	public boolean closeWindow(int windowNo)
	{
		Tabbox tabbox = windowContainer.getComponent();
		Tabpanels panels = tabbox.getTabpanels();
		List<?> childrens = panels.getChildren();
		for (Object child : childrens)
		{
			Tabpanel panel = (Tabpanel) child;
			Component component = panel.getFirstChild();
			Object att = component.getAttribute(WINDOWNO_ATTRIBUTE);
			if (att != null && (att instanceof Integer))
			{
				if (windowNo == (Integer)att)
				{
					Tab tab = (Tab) panel.getLinkedTab();
					panel.getLinkedTab().onClose();
					if (tab.getParent() == null)
					{
						unregisterWindow(windowNo);
						persistOpenTabsFromUI();
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * invoke before a new tab is added to the desktop
	 */
	protected void preOpenNewTab()
	{
	}

	public void restoreSavedTabs()
	{
		SavedTabsState state = getSavedTabsState();
		if (state == null || state.tabs.isEmpty())
			return;

		List<SavedTabState> tabs = new ArrayList<SavedTabState>(state.tabs);
		restoringSavedTabs = true;
		try
		{
			for (SavedTabState savedTab : tabs)
				restoreSavedTab(savedTab);

			Tabbox tabbox = windowContainer.getComponent();
			int maxIndex = tabbox.getTabs().getChildren().size() - 1;
			if (maxIndex >= 0)
			{
				int selectedIndex = Math.max(0, Math.min(state.selectedIndex, maxIndex));
				tabbox.setSelectedIndex(selectedIndex);
			}
		}
		finally
		{
			restoringSavedTabs = false;
			persistOpenTabsFromUI();
		}
	}

	public static void clearSavedTabs(HttpSession httpSession)
	{
		if (httpSession != null)
			httpSession.removeAttribute(SAVED_TABS_ATTRIBUTE);
	}

	private void restoreSavedTab(SavedTabState savedTab)
	{
		if (savedTab == null)
			return;

		if (TAB_TYPE_WINDOW.equals(savedTab.type))
			openWindow(savedTab.id);
		else if (TAB_TYPE_FORM.equals(savedTab.type))
			openForm(savedTab.id);
		else if (TAB_TYPE_BROWSE.equals(savedTab.type))
			openBrowse(savedTab.id, Boolean.valueOf(savedTab.soTrx));
		else if (TAB_TYPE_WORKFLOW.equals(savedTab.type))
			openWorkflow(savedTab.id);
		else if (TAB_TYPE_PROCESS.equals(savedTab.type))
			openProcessDialog(savedTab.id, savedTab.soTrx);
	}

	private void rememberOpenedTab(SavedTabState savedTab)
	{
		tagSelectedTab(savedTab);
		if (!restoringSavedTabs)
			persistOpenTabsFromUI();
	}

	private void tagSelectedTab(SavedTabState savedTab)
	{
		final Tab selectedTab = windowContainer.getSelectedTab();
		if (selectedTab == null)
			return;

		selectedTab.setAttribute(SAVED_TAB_ATTRIBUTE, savedTab);
		org.zkoss.zul.Tabpanel linkedPanel = selectedTab.getLinkedPanel();
		if (linkedPanel != null)
			linkedPanel.setAttribute(SAVED_TAB_ATTRIBUTE, savedTab);

		selectedTab.setAttribute(Tab.AFTER_CLOSE_ATTRIBUTE, new Runnable() {
			public void run() {
				persistOpenTabsFromUI();
			}
		});
		selectedTab.addEventListener(Events.ON_SELECT, new EventListener() {
			public void onEvent(Event event) throws Exception {
				persistOpenTabsFromUI();
			}
		});
	}

	private void persistOpenTabsFromUI()
	{
		HttpSession httpSession = getHttpSession();
		if (httpSession == null || windowContainer == null || windowContainer.getComponent() == null)
			return;

		Tabbox tabbox = windowContainer.getComponent();
		SavedTabsState state = new SavedTabsState();
		state.selectedIndex = tabbox.getSelectedIndex();
		List<?> tabs = tabbox.getTabs().getChildren();
		for (int i = 1; i < tabs.size(); i++)
		{
			Object child = tabs.get(i);
			if (!(child instanceof org.zkoss.zul.Tab))
				continue;

			org.zkoss.zul.Tab tab = (org.zkoss.zul.Tab) child;
			Object savedTab = tab.getAttribute(SAVED_TAB_ATTRIBUTE);
			if (!(savedTab instanceof SavedTabState))
			{
				org.zkoss.zul.Tabpanel linkedPanel = tab.getLinkedPanel();
				if (linkedPanel != null)
					savedTab = linkedPanel.getAttribute(SAVED_TAB_ATTRIBUTE);
			}
			if (savedTab instanceof SavedTabState)
				state.tabs.add((SavedTabState) savedTab);
		}
		httpSession.setAttribute(SAVED_TABS_ATTRIBUTE, state);
	}

	private SavedTabsState getSavedTabsState()
	{
		HttpSession httpSession = getHttpSession();
		if (httpSession == null)
			return null;

		Object state = httpSession.getAttribute(SAVED_TABS_ATTRIBUTE);
		if (state instanceof SavedTabsState)
			return (SavedTabsState) state;
		return null;
	}

	private HttpSession getHttpSession()
	{
		if (Executions.getCurrent() == null || Executions.getCurrent().getDesktop() == null)
			return null;

		Session session = Executions.getCurrent().getDesktop().getSession();
		if (session == null)
			return null;

		Object nativeSession = session.getNativeSession();
		if (nativeSession instanceof HttpSession)
			return (HttpSession) nativeSession;
		return null;
	}

	private static class SavedTabsState implements Serializable
	{
		private static final long serialVersionUID = 7316368185662761181L;

		private int selectedIndex = 0;
		private List<SavedTabState> tabs = new ArrayList<SavedTabState>();
	}

	private static class SavedTabState implements Serializable
	{
		private static final long serialVersionUID = -6539560849248303128L;

		private String type;
		private int id;
		private boolean soTrx;

		private SavedTabState(String type, int id, boolean soTrx)
		{
			this.type = type;
			this.id = id;
			this.soTrx = soTrx;
		}
	}
}
