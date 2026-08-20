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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.adempiere.core.domains.models.X_AD_FieldGroup;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.component.*;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.SimpleTreeModel;
import org.adempiere.webui.component.Tab;
import org.adempiere.webui.component.Tabbox;
import org.adempiere.webui.component.Tabpanel;
import org.adempiere.webui.component.Tabpanels;
import org.adempiere.webui.component.Tabs;
import org.adempiere.webui.editor.*;
import org.adempiere.webui.event.ContextMenuListener;
import org.adempiere.webui.util.GridTabDataBinder;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.*;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Evaluatee;
import org.compiere.util.Util;
import org.zkoss.zk.au.out.AuFocus;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.West;
import org.zkoss.zul.*;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Row;

/**
 *
 * This class is based on org.compiere.grid.GridController written by Jorg Janke.
 * Changes have been brought for UI compatibility.
 *
 * @author Jorg Janke
 *
 * @author <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date Feb 25, 2007
 * @version $Revision: 0.10 $
 *
 * @author Low Heng Sin
 *
 * @author e-Evolution , victor.perez@e-evolution.com
 *      <li>Implement embedded or horizontal tab panel https://adempiere.atlassian.net/browse/ADEMPIERE-319
 *      <li>New ADempiere 3.8.0 ZK Theme Light  https://adempiere.atlassian.net/browse/ADEMPIERE-320
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/610">
 * 		@see FR [ 610 ] Incorrect Label Align on Window for ZK GUI</a>
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/1063">
 * 		@see BR [ 1063 ] Null pointer exception in field type Account zk</a>
 */
public class ADTabPanel extends Div implements Evaluatee, EventListener, DataStatusListener, IADTabPanel, VetoableChangeListener
{
	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = 6945934489328360251L;

	private static final CLogger logger;

    static
    {
        logger = CLogger.getCLogger(ADTabPanel.class);
    }

    private GridTab           gridTab;

    @SuppressWarnings("unused")
	private GridWindow        gridWindow;

    private AbstractADWindowPanel      windowPanel;

    public int               windowNo;

    public Grid              grid;

    private ArrayList<WEditor> editors = new ArrayList<WEditor>();

    /**
     * Componentes asociados directamente a los editores.
     * Se evita depender del UUID porque puede cambiar al adjuntar
     * el componente al árbol de componentes de ZK.
     */
    private ArrayList<Component> editorComponents = new ArrayList<Component>();

    private boolean			  uiCreated = false;

    public GridPanel		  listPanel;

    private Map<String, List<org.zkoss.zul.Row>> fieldGroupContents = new HashMap<String, List<org.zkoss.zul.Row>>();

    private Map<String, List<org.zkoss.zul.Row>> fieldGroupHeaders = new HashMap<String, List<org.zkoss.zul.Row>>();

    private Map<String, Boolean> fieldGroupCollapsed = new HashMap<String, Boolean>();

    private Map<String, Label> fieldGroupToggles = new HashMap<String, Label>();

	private ArrayList<org.zkoss.zul.Row> rowList;

	private Component formComponent = null;

	private ADTreePanel treePanel = null;
	
	/** Indica si el árbol de la pestaña ya fue inicializado. */
	private boolean treeInitialized = false;

	/** Evento diferido para seleccionar un nodo cuando ZK termine de renderizar. */
	private static final String ON_SELECT_TREE_NODE = "onSelectTreeNode";

	/** Registro pendiente de seleccionar. */
	private Integer pendingTreeRecordId = null;

	private boolean treeSelectionScheduled = false;

	private GridTabDataBinder dataBinder;

	private Map<Integer, Group> includedTab = new HashMap<Integer, Group>();
	private Map<Integer, Groupfoot> includedTabFooter = new HashMap<Integer, Groupfoot>();

	private List<EmbeddedPanel> includedPanel = new ArrayList<EmbeddedPanel>();

	private boolean active = false;

	private Group currentGroup;

	private boolean m_vetoActive = false;

	private CWindowToolbar globalToolbar;

    private boolean isEmbedded = false;

    private boolean isSwitchRow = true;	
    
	private int INC = 30;
	
	private GridPanel quickPanel;

	public CWindowToolbar getGlobalToolbar()
	{
		return globalToolbar;
	}

	public void setGlobalToolbar(CWindowToolbar globalToolbar) {
		this.globalToolbar = globalToolbar;
	}



	public ADTabPanel()
	{
        init();
    }

    private void init()
    {
        initComponents();
    }

    private void initComponents()
    {
        LayoutUtils.addSclass("adtab-content", this);

        this.setHflex("1");
        this.setVflex("1");

        grid = new Grid();
        LayoutUtils.addSclass("adtab-form-grid", grid);

        grid.setHflex("1");
        grid.setVflex("1");

        grid.setStyle("margin:0; padding:0;");
        grid.makeNoStrip();

        listPanel = new GridPanel();

        listPanel.setHflex("1");
        listPanel.setVflex("1");

        listPanel.setADTabPanel(this);

        listPanel.getListbox().addEventListener(Events.ON_DOUBLE_CLICK, this);
        listPanel.addEventListener(Events.ON_FOCUS, this);

        this.addEventListener(Events.ON_CLICK, this);
        this.addEventListener(Events.ON_FOCUS, this);
        this.addEventListener(ON_SELECT_TREE_NODE, this);
    }

    /**
     *
     * @param winPanel
     * @param windowNo
     * @param gridTab
     * @param gridWindow
     */
    public void init(AbstractADWindowPanel winPanel, int windowNo, GridTab gridTab,
            GridWindow gridWindow)
    {
        this.windowNo = windowNo;
        this.gridWindow = gridWindow;
        this.gridTab = gridTab;
        this.windowPanel = winPanel;
        gridTab.addDataStatusListener(this);
        this.dataBinder = new GridTabDataBinder(gridTab);

        this.getChildren().clear();

        int treeId = Env.getContextAsInt(Env.getCtx(), windowNo , gridTab.getTabNo() , "AD_Tree_ID" );
		if (gridTab.isTreeTab() && treeId == 0) {
			treeId = MTree.getDefaultTreeIdFromTableId(Env.getAD_Client_ID(Env.getCtx()), gridTab.getAD_Table_ID());
			if (treeId > 0)
				Env.setContext (Env.getCtx(), windowNo, "AD_Tree_ID",  treeId);
		}
		if (gridTab.isTreeTab() && treeId > 0)
		{
			Borderlayout layout = new Borderlayout();
			layout.setParent(this);
			layout.setHflex("1");
			layout.setVflex("1");
			layout.setStyle("margin:0; padding:0;");
			treePanel = new ADTreePanel(
			        windowNo,
			        !gridTab.isReadOnly()
			        && !gridTab.isReadOnlyFromContext()
			);

			West west = new West();
			west.appendChild(treePanel);
			west.setWidth("300px");
			west.setCollapsible(true);
			west.setSplittable(true);
			west.setAutoscroll(true);
			layout.appendChild(west);

			Center center = new Center();
			center.appendChild(grid);
			layout.appendChild(center);

			formComponent = layout;

			treePanel.getTree().addEventListener(Events.ON_SELECT, this);

			if (gridTab.getTabLevel() == 0) {
			    ensureTreeInitialized();
			}
		}
		else
		{
			this.appendChild(grid);
			formComponent = grid;
		}
        this.appendChild(listPanel);
        listPanel.setVisible(false);
        listPanel.setWindowNo(windowNo);
        listPanel.setADWindowPanel(winPanel);
        gridTab.getTableModel().addVetoableChangeListener(this);
    }

    /**
     * Create UI components if not already created
     */
    public void createUI()
    {
    	if (uiCreated)
    		return;

		uiCreated = true;

    	//setup columns
    	Columns columns = new Columns();
    	grid.appendChild(columns);
    	Column col = new Column();
    	col.setWidth("14%");
    	columns.appendChild(col);
    	col = new Column();
    	col.setWidth("35%");
    	columns.appendChild(col);
    	col = new Column();
    	col.setWidth("14%");
    	columns.appendChild(col);
    	col = new Column();
    	col.setWidth("35%");
    	columns.appendChild(col);
    	col = new Column();
    	col.setWidth("2%");
    	columns.appendChild(col);

    	Rows rows = grid.newRows();
        GridField fields[] = gridTab.getFields();
        org.zkoss.zul.Row row = new Row();
        rows.appendChild(row);

        String currentFieldGroup = null;
        for (int i = 0; i < fields.length; i++)
        {
            GridField field = fields[i];
            if (field.isDisplayed())
            {
            	//included tab
            	if (field.getIncluded_Tab_ID() > 0)
            	{
            		if (row.getChildren().size() == 2)
        			{
        				row.appendChild(createSpacer());
                        row.appendChild(createSpacer());
                        row.appendChild(createSpacer());
                        rows.appendChild(row);
                        if (rowList != null)
            				rowList.add(row);
        			} else if (row.getChildren().size() > 0)
        			{
        				rows.appendChild(row);
        				if (rowList != null)
            				rowList.add(row);
        			}

            		//end current field group
            		if (currentGroup != null) {
            			row = new Groupfoot();
            			rows.appendChild(row);
            			currentGroup = null;
            			currentFieldGroup = null;
            		}

                    row = new Row();
                    row.appendChild(createCell(new Separator(), 5));
                    row.appendChild(new Separator());
                    rows.appendChild(row);

                    row = new Group();
                    Cell cell1 = new Cell();
                    cell1.setColspan(2);

                    Cell cell2 = new Cell();
                    cell2.setColspan(3);

                    row.appendChild(cell1);
                    row.appendChild(cell2);
                    rows.appendChild(row);
                    includedTab.put(field.getIncluded_Tab_ID(), (Group)row);

    				org.zkoss.zul.Div div = new Div();
                    div.setWidth("100%");
                    row = new org.adempiere.webui.component.Row();
                    row.appendChild(createCell(new Separator(), 5));
                    row.appendChild(div);
                    rows.appendChild(row);
                    horizontalIncludedTab.put(field.getIncluded_Tab_ID(),  div);

                    row = new Groupfoot();
                    rows.appendChild(row);
                    includedTabFooter.put(field.getIncluded_Tab_ID(), (Groupfoot)row);

            		for (EmbeddedPanel ep : includedPanel) {
            			if (ep.adTabId == field.getIncluded_Tab_ID()) {
            				ep.group = includedTab.get(ep.adTabId);
            				createEmbeddedPanelUI(ep);
            				((ADTabPanel)ep.tabPanel).autoResize();
            				break;
            			}
            		}
                    //Horizontal
                    for (HorizontalEmbeddedPanel ep : horizontalIncludedPanel)
                    {
                        if (ep.adTabId == field.getIncluded_Tab_ID()) {
                            ep.divComponent = horizontalIncludedTab.get(ep.adTabId);
                            createHorizontalEmbeddedPanelUI(ep);
                            ((ADTabPanel)ep.tabPanel).autoResize();
                            break;
                        }
                    }

                    row = new Row();
            		continue;
            	}

            	//normal field
            	String fieldGroup = field.getFieldGroup();
            	if (fieldGroup != null && fieldGroup.trim().length() > 0)
            	{
            		if (!fieldGroup.equals(currentFieldGroup))
            		{
            			currentFieldGroup = fieldGroup;
            			if (row.getChildren().size() == 2)
            			{
            				row.appendChild(createSpacer());
                            row.appendChild(createSpacer());
                            row.appendChild(createSpacer());
                            rows.appendChild(row);
                            if (rowList != null)
                				rowList.add(row);
                            row = new Row();
            			} else if (row.getChildren().size() > 0)
            			{
            				rows.appendChild(row);
            				if (rowList != null)
                				rowList.add(row);
            				row = new Row();
            			}

            			List<org.zkoss.zul.Row> headerRows = new ArrayList<org.zkoss.zul.Row>();
            			fieldGroupHeaders.put(fieldGroup, headerRows);

        				rowList = new ArrayList<org.zkoss.zul.Row>();
        				fieldGroupContents.put(fieldGroup, rowList);

            			boolean collapsed = X_AD_FieldGroup.FIELDGROUPTYPE_Tab.equals(field.getFieldGroupType()) || field.getIsCollapsedByDefault();
            			fieldGroupCollapsed.put(fieldGroup, Boolean.valueOf(collapsed));
            			row = createFieldGroupHeader(fieldGroup, collapsed);
            			rows.appendChild(row);
            			headerRows.add(row);

            			row = new Row();
            		}
            	}

                if (!field.isSameLine() || field.isLongField())
                {
                	//next line
                	if(row.getChildren().size() > 0)
                	{
	                    if (row.getChildren().size() == 2)
	                    {
	                        row.appendChild(createSpacer());
	                        row.appendChild(createSpacer());
	                        row.appendChild(createSpacer());
	                    }
	                    {
	                    	row.appendChild(createSpacer());
	                    }
	                    rows.appendChild(row);
	                    if (rowList != null)
	        				rowList.add(row);
	                    row = new Row();
                	}
                }
                else if (row.getChildren().size() == 4)
                {
                	//next line if reach max column ( 4 )
                	row.appendChild(createSpacer());
                	rows.appendChild(row);
                    if (rowList != null)
        				rowList.add(row);
                    row = new Row();
                }

                WEditor editor = WebEditorFactory.getEditor(gridTab, field, false);


                if (editor != null) // Not heading
                {
                    editor.setGridTab(this.getGridTab());
                    editor.setADTabPanel(this);

                    field.addPropertyChangeListener(editor);
                    editors.add(editor);
                    editorComponents.add(editor.getComponent());
                    if (field.isFieldOnly())
                    {
                    	row.appendChild(createSpacer());
                    }
                    else
                    {
                    	Div div = new Div();
                    	div.setSclass("field-label");
                    	div.setStyle("text-align: right;");

                        Label label = editor.getLabel();
                        div.appendChild(label);


	                    if (label.getDecorator() != null)
	                    	div.appendChild(label.getDecorator());
	                    row.appendChild(div);
                    }
                    row.appendChild(editor.getComponent());
                    if (field.isLongField())
                    {
                        org.zkoss.zul.Cell editorCell = new org.zkoss.zul.Cell();
                        editorCell.setColspan(3);
                        editorCell.setHflex("1");
                        editorCell.appendChild(editor.getComponent());

                        row.appendChild(editorCell);
                        row.appendChild(createSpacer());
                        rows.appendChild(row);

                        if (rowList != null)
                            rowList.add(row);

                        row = new Row();
                    }
                    else
                    {
                        row.appendChild(editor.getComponent());
                    }

                    if (editor instanceof WButtonEditor)
                    {
                    	if (windowPanel != null)
                    		((WButtonEditor)editor).addActionListener(windowPanel);
                    }
                    else
                    {
                    	editor.addValueChangeListener(dataBinder);
                    }

                    //streach component to fill grid cell
                    editor.fillHorizontal();

                    //setup editor context menu
                    WEditorPopupMenu popupMenu = editor.getPopupMenu();
                    if (popupMenu != null)
                    {
                    	//popupMenu.addMenuListener((ContextMenuListener)editor);
                    	 if (editor instanceof ContextMenuListener) {
                    	        popupMenu.addMenuListener((ContextMenuListener) editor);
                    	    }

                        this.appendChild(popupMenu);
                        if (!field.isFieldOnly())
                        {
                        	Label label = editor.getLabel();
	                        if (popupMenu.isZoomEnabled() && editor instanceof IZoomableEditor)
	                        {
	                        	label.setStyle("cursor: pointer; text-decoration: underline;");
	                        	label.addEventListener(Events.ON_CLICK, new ZoomListener((IZoomableEditor) editor));
	                        }

	                        label.setContext(popupMenu.getId());
                        }
                    }
                }
                else if (field.isHeading())
                {
    				//display just a label if we are "heading only"
    				Label label = new Label(field.getHeader());
    				Div div = new Div();
    				div.setStyle("text-align: center;");
    				row.appendChild(createSpacer());
    				div.appendChild(label);
    				row.appendChild(div);
    			}
            }
        }

        //last row
        if (row.getChildren().size() > 0)
        {
            if (row.getChildren().size() == 2)
            {
                row.appendChild(createSpacer());
                row.appendChild(createSpacer());
                row.appendChild(createSpacer());
            }
            rows.appendChild(row);
            if (rowList != null)
				rowList.add(row);
        }
        //create tree
		//	Yamel Senih [ 9223372036854775807 ]
		//	Add support to dynamic tree
//        if (gridTab.isTreeTab() && treePanel != null) {
//			int AD_Tree_ID = MTree.getDefaultAD_Tree_ID (
//				Env.getAD_Client_ID(Env.getCtx()), gridTab.getKeyColumnName());
//			treePanel.initTree(AD_Tree_ID, windowNo);
//        }
        /*if (gridTab.isTreeTab() && treePanel != null) {
        	String treeName = "AD_Tree_ID";
        	int treeId = Env.getContextAsInt (Env.getCtx(), windowNo, treeName, true);
        	//	Valid Tree Value from context
        	if(treeId == 0) {
        		treeId = MTree.getDefaultTreeIdFromTableId(Env.getAD_Client_ID(Env.getCtx()), gridTab.getAD_Table_ID());
        	}
			//	Where
        	treePanel.initTree(treeId, gridTab.getWhereExtended());
        }*/
        ensureTreeInitialized();
        //	End Yamel Senih
        if (!gridTab.isSingleRow() && !isGridView() && !gridTab.isQuickEntry())
        	switchRowPresentation();
        dynamicDisplay(-1);
    }

    private org.zkoss.zul.Row createFieldGroupHeader(final String fieldGroup, boolean collapsed)
    {
    	org.zkoss.zul.Row headerRow = new Row();
    	LayoutUtils.addSclass("adtab-fieldgroup", headerRow);
    	LayoutUtils.addSclass("adtab-fieldgroup-toggle", headerRow);

    	Cell cell = new Cell();
    	cell.setColspan(5);

    	Div header = new Div();
    	LayoutUtils.addSclass("adtab-fieldgroup-title", header);

    	Label toggle = new Label(collapsed ? "\u25b8" : "\u25be");
    	LayoutUtils.addSclass("adtab-fieldgroup-caret", toggle);
    	fieldGroupToggles.put(fieldGroup, toggle);

    	Label label = new Label(fieldGroup);
    	LayoutUtils.addSclass("adtab-fieldgroup-text", label);

    	header.appendChild(toggle);
    	header.appendChild(label);
    	cell.appendChild(header);
    	headerRow.appendChild(cell);
    	headerRow.addEventListener(Events.ON_CLICK, new EventListener() {
			public void onEvent(Event event) throws Exception {
				toggleFieldGroup(fieldGroup);
			}
		});

    	return headerRow;
    }

    private void toggleFieldGroup(String fieldGroup)
    {
    	boolean collapsed = !isFieldGroupCollapsed(fieldGroup);
    	fieldGroupCollapsed.put(fieldGroup, Boolean.valueOf(collapsed));
    	updateFieldGroupToggle(fieldGroup);
    	applyFieldGroupCollapsed(fieldGroup);
    }

    private boolean isFieldGroupCollapsed(String fieldGroup)
    {
    	Boolean collapsed = fieldGroupCollapsed.get(fieldGroup);
    	return collapsed != null && collapsed.booleanValue();
    }

    private void updateFieldGroupToggle(String fieldGroup)
    {
    	Label toggle = fieldGroupToggles.get(fieldGroup);
    	if (toggle != null)
    		toggle.setValue(isFieldGroupCollapsed(fieldGroup) ? "\u25b8" : "\u25be");
    }

    private void applyFieldGroupCollapsed(String fieldGroup)
    {
    	List<org.zkoss.zul.Row> contents = fieldGroupContents.get(fieldGroup);
    	if (contents == null)
    		return;

    	boolean collapsed = isFieldGroupCollapsed(fieldGroup);
    	for (org.zkoss.zul.Row row : contents)
    	{
    		if (collapsed)
    		{
    			if (row.isVisible())
    				row.setVisible(false);
    		}
    		else
    		{
    			Object rowVisible = row.getAttribute("adtab-row-visible");
    			boolean visible = !(rowVisible instanceof Boolean) || ((Boolean)rowVisible).booleanValue();
    			if (row.isVisible() != visible)
    				row.setVisible(visible);
    		}
    	}
    	updateFieldGroupToggle(fieldGroup);
    }

	private Component createSpacer() {
		return new Space();
	}

	/**
	 * Validate display properties of fields of current row
	 * @param col
	 */
    public void dynamicDisplay (int col)
    {
        if (!gridTab.isOpen())
        {
            return;
        }

        for (WEditor comp : editors)
        {
        	comp.setMandatoryLabels();
        }

        //  Selective
        if (col > 0)
        {
            GridField changedField = gridTab.getField(col);
            String columnName = changedField.getColumnName();
            ArrayList<?> dependants = gridTab.getDependantFields(columnName);
            logger.config("(" + gridTab.toString() + ") "
                + columnName + " - Dependents=" + dependants.size());
            if (dependants.size() == 0 && changedField.getCallout().length() == 0)
            {
                return;
            }
        }

        boolean noData = gridTab.getRowCount() == 0;
        logger.config(gridTab.toString() + " - Rows=" + gridTab.getRowCount());
        for (WEditor comp : editors)
        {
            GridField mField = comp.getGridField();
            if (mField != null && mField.getIncluded_Tab_ID() <= 0)
            {
            	if ("AD_Form_ID".equals(mField.getColumnName())
            			|| "AD_Window_ID".equals(mField.getColumnName()))
            	{
            		logger.warning(
            			mField.getColumnName()
            			+ " DisplayLogic=" + mField.getDisplayLogic()
            			+ " ActionContext=" + mField.get_ValueAsString("Action")
            			+ " Displayed=" + mField.isDisplayed(true)
            		);
            	}
                if (mField.isDisplayed(true))       //  check context
                {
                    if (!comp.isVisible())
                    {
                        comp.setVisible(true);      //  visibility
                    }
                    if (noData)
                    {
                        comp.setReadWrite(false);
                    }
                    else
                    {
                        boolean rw = mField.isEditable(true);   //  r/w - check Context
                        comp.setReadWrite(rw);
                        comp.setMandatory(mField.isMandatory(true));    //  check context
                        comp.dynamicDisplay();
                    }
                    //
                    comp.repaintComponent();
                }
                else if (comp.isVisible())
                {
                    comp.setVisible(false);
                }
            }
        }   //  all components

        //hide row if all editor within the row is invisible
        List<?> rows = grid.getRows().getChildren();
        for(int i = 0; i < rows.size(); i++)
        {
        	org.zkoss.zul.Row row = (org.zkoss.zul.Row) rows.get(i);
        	List<?> components = row.getChildren();
        	boolean visible = false;
        	boolean editorRow = false;
        	for (int j = 0; j < components.size(); j++)
        	{
        		Component component = (Component) components.get(j);
        		Component editorComponent = component;

        		if (component instanceof org.zkoss.zul.Cell
        		        && component.getFirstChild() != null)
        		{
        		    editorComponent = component.getFirstChild();
        		}

        		if (editorComponents.contains(editorComponent))
        		{
        		    editorRow = true;

        		    if (editorComponent.isVisible())
        		    {
        		        visible = true;
        		        break;
        		    }
        		}
        	}
        	if (editorRow)
        	{
        		row.setAttribute("adtab-row-visible", Boolean.valueOf(visible));
        		if (row.isVisible() != visible)
        			row.setVisible(visible);
        	}
        }

        //hide fieldgroup if all editor row within the fieldgroup is invisible
        for(Iterator<Entry<String, List<org.zkoss.zul.Row>>> i = fieldGroupHeaders.entrySet().iterator(); i.hasNext();)
        {
        	Map.Entry<String, List<org.zkoss.zul.Row>> entry = i.next();
        	List<org.zkoss.zul.Row> contents = fieldGroupContents.get(entry.getKey());
        	boolean visible = false;
        	for (org.zkoss.zul.Row row : contents)
        	{
        		Object rowVisible = row.getAttribute("adtab-row-visible");
        		if ((rowVisible instanceof Boolean && ((Boolean)rowVisible).booleanValue()) ||
        				(!(rowVisible instanceof Boolean) && row.isVisible()))
        		{
        			visible = true;
        			break;
        		}
        	}
        	List<org.zkoss.zul.Row> headers = entry.getValue();
        	for(org.zkoss.zul.Row row : headers)
        	{
        		if (row.isVisible() != visible)
        			row.setVisible(visible);
        	}
        	applyFieldGroupCollapsed(entry.getKey());
        }

        for (EmbeddedPanel ep : includedPanel) {

        	if(ep.gridWindow.getTab(ep.tabIndex).isDisplayed())
    		{
    			ep.windowPanel.getParent().setVisible(true);
    			ep.group.setVisible(true);
    		}
        	else {

        		ep.windowPanel.getParent().setVisible(false);
    			ep.group.setVisible(false);
        	}
        }

        //Horizontal
        for (HorizontalEmbeddedPanel ep : horizontalIncludedPanel) {

            if(ep.gridWindow.getTab(ep.tabIndex).isDisplayed())
            {
                ep.windowPanel.getParent().setVisible(true);
                ep.divComponent.setVisible(true);
            }
            else {

                ep.windowPanel.getParent().setVisible(false);
                ep.divComponent.setVisible(false);
            }
        }

        logger.config(gridTab.toString() + " - fini - " + (col<=0 ? "complete" : "seletive"));
    }   //  dynamicDisplay

    /**
     * @return String
     */
    public String getDisplayLogic()
    {
        return gridTab.getDisplayLogic();
    }

    /**
     * @return String
     */
    public String getTitle()
    {
        return gridTab.getName();
    } // getTitle

    /**
     * @param variableName
     */
    public String get_ValueAsString(String variableName)
    {
        return Env.getContext(Env.getCtx(), windowNo, variableName);
    } // get_ValueAsString

    /**
     * @return The tab level of this Tabpanel
     */
    public int getTabLevel()
    {
        return gridTab.getTabLevel();
    }

    /**
     * Is panel need refresh
     * @return boolean
     */
    public boolean isCurrent()
    {
        return gridTab != null ? gridTab.isCurrent() : false;
    }

    /**
     *
     * @return windowNo
     */
    public int getWindowNo()
    {
        return windowNo;
    }

    /**
     * Retrieve from db
     */
    public void query()
    {
    	boolean open = gridTab.isOpen();
        gridTab.query(false);
        if (listPanel.isVisible() && !open)
        	gridTab.getTableModel().fireTableDataChanged();
    }

    /**
     * Retrieve from db
     * @param onlyCurrentRows
     * @param onlyCurrentDays
     * @param maxRows
     */
    public void query (boolean onlyCurrentRows, int onlyCurrentDays, int maxRows)
    {
    	boolean open = gridTab.isOpen();
        gridTab.query(onlyCurrentRows, onlyCurrentDays, maxRows);
        if (listPanel.isVisible() && !open)
        	gridTab.getTableModel().fireTableDataChanged();
    }

    public void setGridTab(GridTab gridTab) 
    {
		this.gridTab = gridTab;
	}
    
    /**
     * @return GridTab
     */
    public GridTab getGridTab()
    {
        return gridTab;
    }

    /**
     * Refresh current row
     */
    public void refresh()
    {
        gridTab.dataRefresh();
    }

    /**
     * Activate/deactivate panel
     * @param activate
     */
    public void activate(boolean activate)
    {

    	if (getGrid() != null && activate)
    	{
	    Grid gridCurrent = getGrid();
	    ((HtmlBasedComponent) gridCurrent).setStyle(
	        "margin:0; padding:0; border-left: 3px solid #009bde;"
	    );
	    gridCurrent.setHflex("1");
	    gridCurrent.setVflex("1");
	}
	else if (getGrid() != null && !activate)
	{
	    Grid gridPrevious = getGrid();
	    ((HtmlBasedComponent) gridPrevious).setStyle(
	        "margin:0; padding:0; border:none;"
	    );
	    gridPrevious.setHflex("1");
	    gridPrevious.setVflex("1");
	}
		if (getListPanel() != null && activate)
		{
			GridPanel gridPanel = getListPanel();
			((HtmlBasedComponent)gridPanel).setStyle("border-left: 3px solid #009bde; "); //border-top: 1px solid #fa962f; border-bottom: 1px solid #fa962f; border-right: 1px solid #fa962f;");
			gridPanel.setHflex("1");
			gridPanel.setVflex("1");
		}
		else if (getListPanel() != null && !activate)
		{
			GridPanel gridPanel = getListPanel();
		    ((HtmlBasedComponent)gridPanel).setStyle("border:none;");
		    gridPanel.setHflex("1");
		    gridPanel.setVflex("1");
		}

        //activate embedded panel
        for(EmbeddedPanel ep : includedPanel)
        {
        	activateChild(activate, ep);
        }
        //Horizontal
        for(HorizontalEmbeddedPanel ep : horizontalIncludedPanel)
        {
            activateChild(activate, ep);
        }

    	active = activate;
        if (listPanel.isVisible()) {
        	if (activate)
        		listPanel.activate(gridTab);
        	else
        		listPanel.deactivate();
        } else {
        	if (activate) {
        		formComponent.setVisible(activate);
        		setFocusToField();
        	}
        }
    }

	private void activateChild(boolean activate, EmbeddedPanel panel) {

		if (activate)
		{
			panel.windowPanel.getADTab().evaluate(null);
			panel.windowPanel.getADTab().setSelectedIndex(0);
			panel.tabPanel.query(false, 0, 0);
		}
	}

	/**
	 * set focus to first active editor
	 */
	private void setFocusToField() {
		WEditor toFocus = null;
		for (WEditor editor : editors) {
			if (editor.isHasFocus() && editor.isVisible() && editor.getComponent().getParent() != null) {
				toFocus = editor;
				break;
			}

			if (toFocus == null) {
				if (editor.isVisible() && editor.isReadWrite() && editor.getComponent().getParent() != null) {
					toFocus = editor;
				}
			}
		}
		if (toFocus != null) {
			Component c = toFocus.getComponent();
			if (c instanceof EditorBox) {
				c = ((EditorBox)c).getTextbox();
			}
			Clients.response(new AuFocus(c));
		}
	}

    /**
     * @param event
     * @see EventListener#onEvent(Event)
     */
    public void onEvent(Event event)
    
    {
    	if (ON_SELECT_TREE_NODE.equals(event.getName())) {

    	    treeSelectionScheduled = false;

    	    Integer recordId = pendingTreeRecordId;
    	    pendingTreeRecordId = null;

    	    if (recordId != null
    	            && treePanel != null
    	            && treePanel.getTree() != null) {

    	        Tree tree = treePanel.getTree();

    	        if (tree.getTreechildren() != null) {
    	            setSelectedNode(recordId.intValue());
    	        } else {
    	            logger.warning(
    	                "Treechildren continúa sin inicializar después "
    	                + "del evento diferido. recordId=" + recordId
    	            );
    	        }
    	    }

    	    return;
    	}
    	if (event.getTarget() instanceof Tab)
    	{
    		Tab tab = (Tab)event.getTarget();
    		for (HorizontalEmbeddedPanel embedded : horizontalIncludedPanel )
    		{
    			if (embedded.gridWindow.getTab(embedded.tabIndex).getName().equals(tab.getLabel()))
    			{
    				if (!getGlobalToolbar().getCurrentPanel().equals(embedded.tabPanel))
    				{
    					getGlobalToolbar().getCurrentPanel().activate(false);
    					getGlobalToolbar().getCurrentPanel().setUnselected(getGlobalToolbar().getCurrentPanel());
    					getGlobalToolbar().getCurrentPanel().setSelected(embedded.tabPanel);
    					embedded.tabPanel.activate(true);

    					if(gridTab.getAD_Tab_ID() != getGlobalToolbar().getCurrentPanel().getGridTab().getAD_Tab_ID())
        				{
        					DataStatusEvent m_DataStatusEvent = new DataStatusEvent(this, gridTab.getRowCount(),
        							false,
        							false, false);
        					windowPanel.dataStatusChanged(m_DataStatusEvent);
        				}
    				}
    				return;
    			}
    		}
    	}

		if(event.getTarget() instanceof IADTabPanel)
		{
			IADTabPanel panel = (IADTabPanel)event.getTarget();
			if (panel.getGlobalToolbar() != null
					&& panel == panel.getGlobalToolbar().getCurrentPanel())
				return;

			IADTabPanel last = panel.getGlobalToolbar().getCurrentPanel();
			last.setUnselected(last);
			last.activate(false);

			panel.setSelected(panel);
			panel.activate(true);
			autoResize();
			if(gridTab.getAD_Tab_ID() != panel.getGridTab().getAD_Tab_ID())
			{
				DataStatusEvent m_DataStatusEvent = new DataStatusEvent(this, gridTab.getRowCount(),
						false,
						false, false);
				windowPanel.dataStatusChanged(m_DataStatusEvent);
			}
		}
		else if (event.getTarget() == listPanel.getListbox())
    	{
			//	BR [ 1063 ]
    		if(isSwitchRowPresentation() && !gridTab.isQuickEntry())
    			this.switchRowPresentation();
    	}

    	else if (event.getTarget() == treePanel.getTree()) {
    		Treeitem item =  treePanel.getTree().getSelectedItem();
    		navigateTo((DefaultTreeNode)item.getValue());
    	}
    }

    public void autoResize()
    {
    	if(windowPanel!=null)
    	{
	    	if(windowPanel.isEmbedded())
			{
				Borderlayout window = ((ADWindowPanel)windowPanel).getComponent();

	    		if(isGridView())
	    		{
	    			resizeEmbeddedWindow(window);
					listPanel.resize();
	    		}
	    		else
	    		{
	    			try{
		    			int addSize = 0;
		    			int size = 0;
		    			if (grid.getRows() != null)
		    			{
			    			for(Object o : grid.getRows().getChildren())
			    			{
			    				if(o instanceof Row )
			    				{
			    					if( ((Row) o).isVisible())
			    					{
			    						size += getComponentSize((Row) o);
			    					}
			    				}
			    				else if(o instanceof org.zkoss.zul.Group)
			    				{
			    					size +=20;
			    				}
			    			}
		    			}

                        size += 25; // 25 = statusbar
	    			size += addSize;
	    			size += doAutoSize();
						resizeEmbeddedWindow(window);
	    			}
	    			catch(Exception e)
	    			{
	    				e.printStackTrace();
	    				//nothing to do, just ignore
	    				resizeEmbeddedWindow(window);
	    			}

	    		}


			}
    	}
    }

    private void resizeEmbeddedWindow(Borderlayout window)
    {
        window.setHflex("1");
        window.setVflex("min");
        window.setHeight(null);
        window.resize();
    }



    public void setUnselected(IADTabPanel panel)
    {
    	if (panel != null)
    	{
    		getGlobalToolbar().setPreviousPanel(panel);
    	}

    }

    public void setSelected(IADTabPanel panel)
    {
    	if (panel != null)
    	{
    		getGlobalToolbar().setCurrentPanel(panel);
    	}
    }

    public void repaintComponents(boolean isRow)
    {
    	for(WEditor editor:editors)
    		editor.repaintComponent(isRow);
    }

    private void navigateTo(DefaultTreeNode value) {
    	MTreeNode treeNode = (MTreeNode) value.getData();
    	//  We Have a TreeNode
		int nodeID = treeNode.getNode_ID();
		//  root of tree selected - ignore
		//if (nodeID == 0)
			//return;

		//  Search all rows for mode id
		int size = gridTab.getRowCount();
		int row = -1;
		for (int i = 0; i < size; i++)
		{
			if (gridTab.getKeyID(i) == nodeID)
			{
				row = i;
				break;
			}
		}
		if (row == -1)
		{
			if (nodeID > 0)
				logger.log(Level.WARNING, "Tab does not have ID with Node_ID=" + nodeID);
			return;
		}

		//  Navigate to node row
		gridTab.navigate(row);
	}

    /**
     * @param e
     * @see DataStatusListener#dataStatusChanged(DataStatusEvent)
     */
	public void dataStatusChanged(DataStatusEvent e)
    {
    	//ignore background event
    	if (Executions.getCurrent() == null) return;

        int col = e.getChangedColumn();
        logger.config("(" + gridTab + ") Col=" + col + ": " + e.toString());

        //  Process Callout
        GridField mField = gridTab.getField(col);
        if (mField != null
            && (mField.getCallout().length() > 0 || gridTab.hasDependants(mField.getColumnName())))
        {
            String msg = gridTab.processFieldChange(mField);     //  Dependencies & Callout
            if (msg.length() > 0)
            {
                FDialog.error(windowNo, this, msg);
            }

            // Refresh the list on dependant fields
    		ArrayList<GridField> list = gridTab.getDependantFields(mField.getColumnName());
    		for (int i = 0; i < list.size(); i++)
    		{
    			GridField dependentField = (GridField)list.get(i);
    		//	log.trace(log.l5_DData, "Dependent Field", dependentField==null ? "null" : dependentField.getColumnName());
    			//  if the field has a lookup
    			if (dependentField != null && dependentField.getLookup() instanceof MLookup)
    			{
    				MLookup mLookup = (MLookup)dependentField.getLookup();
    				//  if the lookup is dynamic (i.e. contains this columnName as variable)
    				if (mLookup.getValidation().indexOf("@"+mField.getColumnName()+"@") != -1)
    				{
    					mLookup.refresh();
    				}
    			}
    		}   //  for all dependent fields

        }
        //if (col >= 0)
        if (!uiCreated)
        	createUI();
        dynamicDisplay(col);

		/*int treeId = Env.getContextAsInt(Env.getCtx(), windowNo , gridTab.getTabNo(), "AD_Tree_ID");
		if ((gridTab.isTreeTab() && treeId == 0) || (gridTab.isTreeTab() && gridTab.getTabLevel() == 0))
			treeId = MTree.getDefaultTreeIdFromTableId(Env.getAD_Client_ID(Env.getCtx()), gridTab.getAD_Table_ID());
		if (gridTab.isTreeTab() && treeId > 0 && treePanel != null) {
			treePanel.initTree(treeId, gridTab.getWhereExtended());
			if (!gridTab.isSingleRow() && !isGridView() && !gridTab.isQuickEntry())
				switchRowPresentation();
		}*/
        ensureTreeInitialized();

        //sync tree
        if (treePanel != null) {
        	if ("Deleted".equalsIgnoreCase(e.getAD_Message()))
        		if (e.Record_ID != null
        				&& e.Record_ID instanceof Integer
        				&& ((Integer)e.Record_ID != gridTab.getRecord_ID()))
        			deleteNode((Integer)e.Record_ID);
        		else
        			setSelectedNode(gridTab.getRecord_ID());
        	else
        		setSelectedNode(gridTab.getRecord_ID());
        }

        if (listPanel.isVisible()) {
        	listPanel.updateListIndex();
        	listPanel.dynamicDisplay(col);
        }

        if (!includedPanel.isEmpty() && e.getChangedColumn() == -1) {
        	for (EmbeddedPanel panel : includedPanel)
        		panel.tabPanel.query(false, 0, 0);
        }
        //Horizontal
        if (!horizontalIncludedPanel.isEmpty() && e.getChangedColumn() == -1) {
            for (HorizontalEmbeddedPanel panel : horizontalIncludedPanel)
                panel.tabPanel.query(false, 0, 0);
        }
    }

	private void deleteNode(int recordId) {
	    if (recordId <= 0) return;

	    Object treeModel = treePanel.getTree().getModel();
	    SimpleTreeModel model = (SimpleTreeModel) treeModel;

	    if (treePanel.getTree().getSelectedItem() != null) {
	        DefaultTreeNode treeNode = (DefaultTreeNode) treePanel.getTree().getSelectedItem().getValue();
	        MTreeNode data = (MTreeNode) treeNode.getData();

	        if (data.getNode_ID() == recordId) {
	            model.removeNode(treeNode);
	            return;
	        }
	    }

	    DefaultTreeNode treeNode = model.find(null, recordId);
	    if (treeNode != null) {
	        model.removeNode(treeNode);
	    }
	}

	private void addNewNode() {
	    if (gridTab.getRecord_ID() > 0) {
	        String name = (String) gridTab.getValue("Name");
	        String description = (String) gridTab.getValue("Description");
	        boolean summary = gridTab.getValueAsBoolean("IsSummary");
	        String imageIndicator = (String) gridTab.getValue("Action");  // Menu - Action

	        Object treeModel = treePanel.getTree().getModel();
	        SimpleTreeModel model = (SimpleTreeModel) treeModel;

	        DefaultTreeNode treeNode = model.getRoot();
	        MTreeNode root = (MTreeNode) treeNode.getData();

	        MTreeNode node = new MTreeNode(
	                gridTab.getRecord_ID(),
	                0,
	                name,
	                description,
	                root.getNode_ID(),
	                summary,
	                imageIndicator,
	                false,
	                null
	        );

	        DefaultTreeNode newNode = new DefaultTreeNode(node);

	        model.addNode(newNode);

	        int[] path = model.getPath(newNode);
	        try {
	        	Tree tree = treePanel.getTree();

	        	if (tree == null || tree.getTreechildren() == null) {
	        	    pendingTreeRecordId = Integer.valueOf(
	        	        gridTab.getRecord_ID()
	        	    );

	        	    if (!treeSelectionScheduled) {
	        	        treeSelectionScheduled = true;
	        	        tree.invalidate();

	        	        Events.echoEvent(
	        	            ON_SELECT_TREE_NODE,
	        	            this,
	        	            null
	        	        );
	        	    }

	        	    return;
	        	}
	            //Treeitem ti = treePanel.getTree().renderItemByPath(path);
	        	Treeitem ti = tree.renderItemByPath(path);
	            

	            if (ti != null) {
	                treePanel.getTree().setSelectedItem(ti);
	            }

	        } catch (Throwable t) {
	            logger.log(Level.WARNING,
	                "Error seleccionando nuevo nodo árbol", t);
	        }
	    }
	}

	/**
	 * Sincroniza el registro actual con el nodo correspondiente del árbol.
	 */
	private void setSelectedNode(int recordId) {

	    if (recordId <= 0 || treePanel == null) {
	        return;
	    }

	    Tree tree = treePanel.getTree();

	    if (tree == null) {
	        return;
	    }

	    Object rawModel = tree.getModel();

	    if (!(rawModel instanceof SimpleTreeModel)) {
	        logger.warning(
	            "Modelo de árbol no disponible o incompatible para recordId="
	            + recordId
	            + ". Modelo="
	            + (rawModel != null
	                ? rawModel.getClass().getName()
	                : "null")
	        );
	        return;
	    }

	    /*
	     * En ZK 10 el modelo puede estar asignado antes de que exista
	     * el Treechildren visual. Se posterga la selección hasta el
	     * siguiente ciclo de eventos.
	     */
	    if (tree.getTreechildren() == null) {

	        pendingTreeRecordId = Integer.valueOf(recordId);

	        if (!treeSelectionScheduled) {
	            treeSelectionScheduled = true;

	            tree.invalidate();

	            Events.echoEvent(
	                ON_SELECT_TREE_NODE,
	                this,
	                null
	            );
	        }

	        return;
	    }

	    Treeitem selectedItem = tree.getSelectedItem();

	    if (selectedItem != null
	            && selectedItem.getValue() instanceof DefaultTreeNode) {

	        DefaultTreeNode selectedNode =
	            (DefaultTreeNode) selectedItem.getValue();

	        if (selectedNode.getData() instanceof MTreeNode) {

	            MTreeNode selectedData =
	                (MTreeNode) selectedNode.getData();

	            if (selectedData.getNode_ID() == recordId) {
	                return;
	            }
	        }
	    }

	    SimpleTreeModel model = (SimpleTreeModel) rawModel;

	    DefaultTreeNode treeNode = model.find(null, recordId);

	    if (treeNode == null) {
	        addNewNode();
	        return;
	    }

	    int[] path = model.getPath(treeNode);

	    if (path == null || path.length == 0) {
	        logger.warning(
	            "Ruta vacía para recordId=" + recordId
	        );
	        return;
	    }

	    try {

	        Treeitem treeItem = tree.renderItemByPath(path);

	        if (treeItem != null) {
	            tree.setSelectedItem(treeItem);
	        } else {
	            logger.warning(
	                "No se pudo renderizar el nodo. recordId="
	                + recordId
	                + ", path="
	                + Arrays.toString(path)
	            );
	        }

	    } catch (RuntimeException ex) {

	        logger.log(
	            Level.WARNING,
	            "Error seleccionando nodo del árbol. recordId="
	            + recordId
	            + ", path="
	            + Arrays.toString(path),
	            ex
	        );
	    }
	}
	
	/**
	 * Set Switch Row Presentation
	 * @param isSwitchRow
	 */
	public void setSwitchRowPresentation(boolean isSwitchRow) {
		this.isSwitchRow = isSwitchRow;
	}
	
	public boolean isSwitchRowPresentation() {
		return this.isSwitchRow;
	}
	
	/**
	 * Toggle between form and grid view
	 */
	public void switchRowPresentation() {
		if (formComponent.isVisible()) {
			formComponent.setVisible(false);
			//de-activate embedded panel
	        for(EmbeddedPanel ep : includedPanel)
	        {
	        	activateChild(false, ep);
	        }
            //Horizontal
            for(HorizontalEmbeddedPanel ep : horizontalIncludedPanel)
            {
                activateChild(false, ep);
            }
		} else {
		    gridTab.dataRefresh(gridTab.getCurrentRow());
			formComponent.setVisible(true);
			repaintComponents(false);
			//activate embedded panel
	        for(EmbeddedPanel ep : includedPanel)
	        {
	        	activateChild(true, ep);
	        }
            //Horizontal
            for(HorizontalEmbeddedPanel ep : horizontalIncludedPanel)
            {
                activateChild(true , ep);
            }
		}
		listPanel.setVisible(!formComponent.isVisible());
		if (listPanel.isVisible()) {

			if (listPanel != null && listPanel.getADTabPanel() != null && listPanel.getADTabPanel().getGlobalToolbar() != null )
				listPanel.getADTabPanel().getGlobalToolbar().setCurrentPanel(this);
			    
				listPanel.refresh(gridTab);
			listPanel.scrollToCurrentRow();
			repaintComponents(true);

		} else {
			listPanel.deactivate();
		}
		autoResize();

	}

	public GridPanel getListPanel()
	{
		return listPanel;
	}
	class ZoomListener implements EventListener {

		private IZoomableEditor searchEditor;

		ZoomListener(IZoomableEditor editor) {
			searchEditor = editor;
		}

		public void onEvent(Event event) throws Exception {
			if (Events.ON_CLICK.equals(event.getName())) {
				searchEditor.actionZoom();
			}

		}

	}

	/**
	 * Embed detail tab
	 * @param ctx
	 * @param windowNo
	 * @param gridWindow
	 * @param adTabId
	 * @param tabIndex
	 * @param tabPanel
	 */
	public void embed(Properties ctx, int windowNo, GridWindow gridWindow,
			int adTabId, int tabIndex, IADTabPanel tabPanel) {
		EmbeddedPanel ep = new EmbeddedPanel();
		ep.tabPanel = tabPanel;
		ep.adTabId = adTabId;
		ep.tabIndex = tabIndex;
		ep.gridWindow = gridWindow;
		includedPanel.add(ep);
		Group group = includedTab.get(adTabId);
		ep.group = group;
		if (tabPanel instanceof ADTabPanel) {
			ADTabPanel atp = (ADTabPanel) tabPanel;
			atp.listPanel.setPageSize(-1);
		}
		ADWindowPanel panel = new ADWindowPanel(ctx, windowNo, gridWindow, tabIndex, tabPanel);
		ep.windowPanel = panel;

		if (group != null) {
			createEmbeddedPanelUI(ep);
			if (active)
				activateChild(true, ep);
		}
	}

	class EmbeddedPanel {
		Group group;
		GridWindow gridWindow;
		int tabIndex;
		ADWindowPanel windowPanel;
		IADTabPanel tabPanel;
		int adTabId;
	}

	/**
	 * @see IADTabPanel#afterSave(boolean)
	 */
	public void afterSave(boolean onSaveEvent) {
		if (!includedPanel.isEmpty()) {
        	for (EmbeddedPanel panel : includedPanel)
        		//panel.tabPanel.getGridTab().dataRefreshAll()
        		panel.tabPanel.query(false, 0, 0);
        }
        //Horizontal
        if (!horizontalIncludedPanel.isEmpty()) {
            for (HorizontalEmbeddedPanel panel : horizontalIncludedPanel)
                //panel.tabPanel.getGridTab().dataRefreshAll()
                panel.tabPanel.query(false, 0, 0);
        }
	}

	private void createEmbeddedPanelUI(EmbeddedPanel ep) {


		org.zkoss.zul.Row row = new Row();
		row.appendChild(createCell(new Separator(), 5));

		if(!ep.gridWindow.getTab(ep.tabIndex).isDisplayed())
		{
			row.setVisible(false);
			ep.group.setVisible(false);
		}

		grid.getRows().insertBefore(row, includedTabFooter.get(ep.adTabId));
		ep.windowPanel.createPart(row);
		ep.windowPanel.getComponent().setHflex("1");
		ep.windowPanel.getComponent().setVflex("min");
		ep.windowPanel.getComponent().setStyle("position: relative");
		// ZK10: embedded tabs use natural flex sizing instead of fixed pixel height.

		Label title = new Label(ep.gridWindow.getTab(ep.tabIndex).getName());
		ep.group.appendChild(title);
		ep.group.appendChild(ep.windowPanel.getToolbar());
		ep.windowPanel.getStatusBar().setZclass("z-group-foot");
		ep.windowPanel.initPanel(-1, null);
	}

	@Override
	public void focus() {
		if (formComponent.isVisible()) {
			this.setFocusToField();
			listPanel.addKeyListener();
		}
		else
			listPanel.focus();
	}

	public void setFocusToField(String columnName) {
		if (formComponent.isVisible()) {
			boolean found = false;
			for (WEditor editor : editors) {
				if (found)
					editor.setHasFocus(false);
				else if (columnName.equals(editor.getColumnName())) {
					editor.setHasFocus(true);
					Clients.response(new AuFocus(editor.getComponent()));
					found = true;
				}
			}
		} else {
			listPanel.setFocusToField(columnName);
		}
	}

	public void updateMandatoryLabels() {
		if (formComponent.isVisible()) {
			
			for (WEditor editor : editors) {
				if(editor.isMandatoryStyle())
					editor.setMandatoryStyle();
			}
		} 
	}

	/**
	 * @see IADTabPanel#onEnterKey()
	 */
	public boolean onEnterKey() {
		if (listPanel.isVisible()) {
			return listPanel.onEnterKey();
		}
		return false;
	}

	/**
	 * @param e
	 * @see VetoableChangeListener#vetoableChange(PropertyChangeEvent)
	 */
	public void vetoableChange(PropertyChangeEvent e)
			throws PropertyVetoException {
		//  Save Confirmation dialog    MTable-RowSave
		if (e.getPropertyName().equals(GridTable.PROPERTY))
		{
			//  throw new PropertyVetoException will call this listener again to revert to old value
			if (m_vetoActive)
			{
				//ignore
				m_vetoActive = false;
				return;
			}
			if (!Env.isAutoCommit(Env.getCtx(), getWindowNo()) || gridTab.getCommitWarning().length() > 0)
			{
				if (!FDialog.ask(getWindowNo(), this, "SaveChanges?", gridTab.getCommitWarning()))
				{
					m_vetoActive = true;
					throw new PropertyVetoException ("UserDeniedSave", e);
				}
			}
			return;
		}   //  saveConfirmation
	}

	/**
	 * @return boolean
	 */
	public boolean isGridView() {
		return listPanel.isVisible();
	}

	/**
	 * @param gTab
	 * @return embedded panel or null if not found
	 */
	public IADTabPanel findEmbeddedPanel(GridTab gTab) {
		IADTabPanel panel = null;
		for(EmbeddedPanel ep : includedPanel) {
			if (ep.tabPanel.getGridTab().equals(gTab)) {
				return ep.tabPanel;
			}
		}
        //Horizontal
        for(HorizontalEmbeddedPanel ep : horizontalIncludedPanel) {
            if (ep.tabPanel.getGridTab().equals(gTab)) {
                return ep.tabPanel;
            }
        }

		return panel;
	}

	/**
	 *
	 * @return GridPanel
	 */
	public GridPanel getGridView() {
		return listPanel;
	}

	public int doAutoSize()
	{
			int size = 0;
			for(EmbeddedPanel panel : includedPanel)
			{
				size += includedAutoResize(panel);
			}

            for(HorizontalEmbeddedPanel panel : horizontalIncludedPanel)
            {
                size += includedAutoResize(panel);
            }
            return size;
	}

	public int includedAutoResize(EmbeddedPanel embeddedpanel)
	{
		if(embeddedpanel.windowPanel  !=null)
    	{
	    	if(embeddedpanel.windowPanel.isEmbedded())
			{
				Borderlayout window = embeddedpanel.windowPanel.getComponent();
	    			try{

	    				int size = 0;
	    				int addSize = 0;
	    				if (!embeddedpanel.tabPanel.getGridTab().isSingleRow())
	    				{
	    					size = MSysConfig.getIntValue("TAB_INCLUDING_HEIGHT", 400);
	    				}
	    				else {

	    					for(Object o : embeddedpanel.tabPanel.getGrid().getRows().getChildren())
			    			{
			    				if(o instanceof Row)
			    				{
			    					if( ((Row) o).isVisible())
			    					{
			    						size += getComponentSize((Row) o) ;
			    					}
			    				}
			    				else if (o instanceof org.zkoss.zul.Group)
			    				{
			    					size +=20; // Group
			    				}

			    			}
			    		    size += 25; // 25 = statusbar
	    	    			size += addSize;
			    			List<EmbeddedPanel> included = embeddedpanel.tabPanel.getIncludedPanel();
			    			if(included.size()>0)
			    			{
			    				for(EmbeddedPanel panel : included)
			    				{
				    				size += includedAutoResize(panel);
			    				}
			    			}
	    				}

					    resizeEmbeddedWindow(window);
	    			    return size;
	    			}
	    			catch(Exception e)
	    			{
	    				e.printStackTrace();
	    			}

	    			return 0;
			}

    	}
		return 0;
	}

    public int includedAutoResize(HorizontalEmbeddedPanel embeddedPanel)
    {
        if(embeddedPanel.windowPanel  !=null)
        {
            if(embeddedPanel.windowPanel.isEmbedded())
            {
                Borderlayout window = embeddedPanel.windowPanel.getComponent();
                try{
                    int size = 0;
                    int addSize = 0;
                    if (!embeddedPanel.tabPanel.getGridTab().isSingleRow())
                    {
                        size = MSysConfig.getIntValue("TAB_INCLUDING_HEIGHT", 400);
                    }
                    else {
                        for(Object o : embeddedPanel.tabPanel.getGrid().getRows().getChildren())
                        {
                            if(o instanceof Row)
                            {
                                if( ((Row) o).isVisible())
                                {
                                    size += getComponentSize((Row) o) ;
                                }
                            }
                            else if (o instanceof org.zkoss.zul.Div)
                            {
                                size +=20; // Group
                            }

                        }
                        size += 25; // 25 = statusbar

                        size += addSize;
                        List<HorizontalEmbeddedPanel> included = embeddedPanel.tabPanel.getHorizontalIncludedPanel();
                        if(included.size()>0)
                        {
                            for(HorizontalEmbeddedPanel panel : included)
                            {
                                size += includedAutoResize(panel);
                            }
                        }
                    }

                    resizeEmbeddedWindow(window);
                    return size;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                return 0;
            }

        }
        return 0;
    }

	private int getComponentSize(Row row) {

		int addSize = 0;

		for (Object o : ((Row) row).getChildren()) {

			if(o instanceof org.zkoss.zul.Borderlayout)
			{
				return 0;
			}

			if (o instanceof org.zkoss.zk.ui.HtmlBasedComponent  ){

				String height = ((org.zkoss.zk.ui.HtmlBasedComponent) o).getHeight();

				if (height==null)
				{
					height="30";
				}

				if(!height.contains("%"))
				{

					int size = Integer.parseInt(height.replace("px", "")) + 6;

					if (size > addSize)
						addSize = size;
				}

			}
		}

		return addSize;
	}

	@Deprecated
	private int sizeImage(Row row) {

		int addSize = 0;

		for (Object o : ((Row) row).getChildren()) {

			if (o instanceof org.zkoss.zul.Image){

				int size = Integer.parseInt(((org.zkoss.zul.Image) o)
						.getHeight().replace("px", "")) - INC;

				if (size > addSize)
					addSize = size;
			}
		}

		return addSize;
	}

	@Override
	public Grid getGrid() {
		return grid;
	}

    public void setIsEmbedded(boolean isEmbedded)
    {
        this.isEmbedded=isEmbedded;
    }

    public boolean isEmbedded()
    {
        return isEmbedded;
    }

	@Override
	public List<EmbeddedPanel> getIncludedPanel() {
		return includedPanel;
	}

    public List<HorizontalEmbeddedPanel> getHorizontalIncludedPanel() {
        return horizontalIncludedPanel;
    }


    private List<HorizontalEmbeddedPanel> horizontalIncludedPanel = new ArrayList<HorizontalEmbeddedPanel>();
    private Map<Integer, org.zkoss.zul.Div> horizontalIncludedTab = new HashMap<Integer, org.zkoss.zul.Div>();
    private Map<Integer, Tabpanel> embeddedTabPanel = new HashMap<Integer, Tabpanel>();

    private Tabbox tabBox	= null;
    private Tabs tabs	= null;
    private Tabpanels tabPanels	= null;

    class HorizontalEmbeddedPanel {
        org.zkoss.zul.Div divComponent ;
        org.zkoss.zul.Row toolbarRow ;
        Grid embeddedGrid;
        GridWindow gridWindow;
        int tabIndex ;
        ADWindowPanel windowPanel;
        IADTabPanel tabPanel;
        int adTabId;
        Panelchildren panelChildren;
    }

    /**
     * Embed detail tab
     * @param ctx
     * @param windowNo
     * @param gridWindow
     * @param adTabId
     * @param tabIndex
     * @param tabPanel
     */
    public void HorizontalEmbedded(Properties ctx, int windowNo, GridWindow gridWindow,
                      int adTabId, int tabIndex, IADTabPanel tabPanel) {
        HorizontalEmbeddedPanel ep = new HorizontalEmbeddedPanel();
        ep.tabPanel = tabPanel;
        ep.adTabId = adTabId;
        ep.tabIndex = tabIndex;
        ep.gridWindow = gridWindow;
        org.zkoss.zul.Div parentRow = horizontalIncludedTab.get(adTabId );
        ep.divComponent = parentRow;
        horizontalIncludedPanel.add(ep);
        if (tabPanel instanceof ADTabPanel) {
            ADTabPanel atp = (ADTabPanel) tabPanel;
            atp.listPanel.setPageSize(-1);
        }
        ADWindowPanel panel = new ADWindowPanel(ctx, windowNo, gridWindow, tabIndex, tabPanel);
        ep.windowPanel = panel;


        if (parentRow != null) {
            createHorizontalEmbeddedPanelUI(ep);
            if (active)
                activateChild(true, ep);
        }
    }

    private void activateChild(boolean activate, HorizontalEmbeddedPanel panel) {
        if (activate)
        {
            panel.windowPanel.getADTab().evaluate(null);
            panel.windowPanel.getADTab().setSelectedIndex(0);
            panel.tabPanel.query(false, 0, 0);
        }

       /* panel.tabPanel.activate(activate);
        if (activate)
        {
            activateTabPanel(panel);
        }*/
    }

//    private void activateTabPanel(HorizontalEmbeddedPanel panel) {
//
//        if( tabPanels != null )
//
//        panel.divComponent.setVisible(true);
////			panel.divComponent.setStyle("position: relative; overflow:auto; ");
//
//
//        tabPanels.setVisible(true);
//        tabPanels.setStyle("margin:0; padding:0; border: none; position: relative; ");
//
////			embeddTabPanel.get(panel.adTabId).setVisible(true);
//        //embeddedTabPanel.get(panel.adTabId).setStyle(" margin:0; padding:0; border: none; height: 600px; ");
//        embeddedTabPanel.get(panel.adTabId).setStyle(" margin:0; padding:0; border: none; position: relative; ");
//
//        panel.panelChildren.setVisible(true);
//        panel.panelChildren.setStyle(" margin:0; padding:0; border: none; position: relative;  ");
//        //panel.panelChildren.setStyle(" margin:0; padding:0; border: none; height: 600px; ");
//
//
//        panel.embeddedGrid.setVisible(true);
//       // panel.embeddedGrid.setStyle("border: none; height: 600px;  ");
//        panel.embeddedGrid.setStyle("border: none; position: relative; ");
//
//    }

    private void createHorizontalEmbeddedPanelUI(HorizontalEmbeddedPanel ep) {

        org.zkoss.zul.Row ChildRow = createHorizontalPanelForEmbedded(ep.divComponent, includedTabFooter.get(ep.adTabId), ep);

        ep.windowPanel.createPart(ChildRow);
        ep.windowPanel.getComponent().setHflex("1");
        ep.windowPanel.getComponent().setVflex("min");
        //ep.windowPanel.getComponent().setStyle("position: relative");
        ep.windowPanel.getComponent().setHeight(null);

        FToolbar bar = ep.windowPanel.getToolbar();
        bar.setAlign("start");
        bar.setStyle("background-color: transparent; height: 20%; ");

        ep.toolbarRow.appendChild(bar);
        ep.windowPanel.getStatusBar().setZclass("z-group-foot");
        ep.windowPanel.initPanel(-1, null);
    }

    private org.zkoss.zul.Row createHorizontalPanelForEmbedded(org.zkoss.zul.Div divComponent, org.zkoss.zul.Row footer , HorizontalEmbeddedPanel ep ) {
        //Setting Properties to Div Component
        divComponent.setHflex("1");
        divComponent.setVflex("min");
        // Create a Panel Object
        Panel panel = new Panel();
        panel.setStyle("border: 1px solid #d9d9d9; background: #ffffff;");
    	//panel.setStyle("overflow:auto");
        panel.setHflex("1");
        panel.setVflex("min");
        panel.setMaximizable(true);


        //creating Object to PanelChildren class
        ep.panelChildren = new Panelchildren();
        // Creating a Object to Grid And Apply Properties
        Grid newGrid = new Grid();
        newGrid.setHflex("1");
        newGrid.setVflex("1");
        newGrid.setStyle("margin:0; padding:0; border: none;");
        newGrid.makeNoStrip();
        // Grid append to Panel Children
        ep.panelChildren.appendChild( newGrid );
        // Panel Children Append to Panel
        panel.appendChild(ep.panelChildren);
        // Creating one Tabbox And panel append to TabPanels
        Tabbox panels =  setTabPanels(ep, panel) ;
        // TabBox Append to divComponent
        panels.setParent( divComponent );
        ep.embeddedGrid = newGrid;
        //Creating Rows based on the Grid
        Rows newRows = newGrid.newRows();
        newRows.setVflex("min");
        org.zkoss.zul.Row newRow = new Group();
        // Create a Row For ToolBar
        org.zkoss.zul.Row toolbarRow = new org.adempiere.webui.component.Row();
        toolbarRow.appendChild(createCell(new Separator(), 5));
        ep.toolbarRow = toolbarRow;
        //Create a Row For All Widgets
        org.zkoss.zul.Row panelRow = new org.adempiere.webui.component.Row();
        panelRow.appendChild(createCell(new Separator(), 5));
        //panelRow.setWidth("100%");
        panelRow.setVflex("min");
        // Added to Group
        newRows.appendChild( newRow );
        // Added to tool-bar Row
        newRows.appendChild(toolbarRow);
        // Now added to Panel Row
        newRows.appendChild( panelRow );
        //Added to footer
        newRows.appendChild( footer );


        return panelRow ;
    }

    private Tabbox setTabPanels(HorizontalEmbeddedPanel ep, Panel panel  ) {
        // For One Tab We created only one TabBox
        if (null == tabBox && null == tabPanels) {
            tabBox = new Tabbox();
            tabBox.setHflex("1");
            tabBox.setVflex("min");
            //tabBox.setStyle("height: 100%; width: 100%; position: relative;");
            tabPanels = new Tabpanels();
            tabPanels.setHflex("1");
            tabPanels.setVflex("min");
            tabBox.appendChild(tabPanels);
            tabs = new Tabs();
            tabBox.appendChild(tabs);

        }
        // Setting Name For Tab
        Tab tab = new Tab(ep.gridWindow.getTab(ep.tabIndex).getName());
        tab.addEventListener(Events.ON_CLICK, this);
        tab.addEventListener(Events.ON_FOCUS, this);
        // Appending Tab to Tabs
        tabs.appendChild(tab);
        //Creating a TabPanel For every Tab
        Tabpanel tabPanel = new Tabpanel();
        embeddedTabPanel.put(ep.adTabId, tabPanel);
        tabPanel.setHflex("1");
        tabPanel.setVflex("min");
        tabPanel.setStyle("margin:0; padding:0; border: none;");
        //Setting Embedded Panel to tabPanel
        tabPanel.appendChild(panel);
        //Appending Tab Panel To TabPanels
        tabPanels.appendChild(tabPanel);
        //By Default first Tab should populated
        tabBox.setSelectedIndex(0);

        // Returning the tabbox
        return tabBox;

    }

    public void setQuickPanel(GridPanel gridPanel) {
    	quickPanel=gridPanel;
    }
    public GridPanel getQuickPanel() {
    	return quickPanel;
    }
    
    /**
     * Change label for each field if it has context info configured
     */
    public void reloadFieldTrxInfo() {
    	for (WEditor comp : editors) {
            GridField mField = comp.getGridField();
            if (mField != null && mField.getIncluded_Tab_ID() <= 0) {
                if (mField.isDisplayed(true)) {       //  check context
                    //	Change Context info
                    reloadFieldTrxInfo(comp);
                }
            }
        }   //  all components
    }
    
    /**
     * Change label for each field if it has context info configured
     */
    private void reloadFieldTrxInfo(WEditor editor) {
    	Map<String, String> contextValues = gridTab.getFieldTrxInfo();
		if(contextValues == null 
				|| contextValues.size() == 0) {
			return;
		}
		//	change fields
		GridField field = editor.getField();
		//	Get trx info
		String messageValue = contextValues.get(field.getColumnName());
		if(Util.isEmpty(messageValue)) {
			return;
		}
		//	Set Context info
		((HtmlBasedComponent) editor.getComponent()).setTooltiptext(messageValue);
    }
    
    private Cell createCell(Component child, int colspan)
    {
        Cell cell = new Cell();
        cell.setColspan(colspan);

        if (child != null)
            cell.appendChild(child);

        return cell;
    }
    /**
     * Inicializa el árbol solamente cuando todavía no tiene modelo.
     */
    private void ensureTreeInitialized() {

        if (treePanel == null || !gridTab.isTreeTab()) {
            return;
        }

        Tree tree = treePanel.getTree();

        if (tree == null) {
            return;
        }

        if (treeInitialized && tree.getModel() != null) {
            return;
        }

        int treeId = Env.getContextAsInt(
                Env.getCtx(),
                windowNo,
                gridTab.getTabNo(),
                "AD_Tree_ID"
        );

        if (treeId <= 0) {
            treeId = MTree.getDefaultTreeIdFromTableId(
                    Env.getAD_Client_ID(Env.getCtx()),
                    gridTab.getAD_Table_ID()
            );
        }

        if (treeId <= 0) {
            logger.warning(
                    "No se encontró AD_Tree_ID para AD_Table_ID="
                    + gridTab.getAD_Table_ID()
            );
            return;
        }

        treePanel.initTree(treeId, gridTab.getWhereExtended());
        treeInitialized = tree.getModel() != null;
    }
}
