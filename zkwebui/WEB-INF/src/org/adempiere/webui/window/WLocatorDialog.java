/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
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
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

/**
 * 2007, Modified by Posterita Ltd.
 */

package org.adempiere.webui.window;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.Window;
import org.compiere.model.MLocator;
import org.compiere.model.MLocatorLookup;
import org.compiere.model.MRole;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Vbox;

/**
 * Location Dialog : Based on VLocationDialog
 * 
 * @author  Niraj Sohun
 * @date    Jul 24, 2007
 */

public class WLocatorDialog extends Window implements EventListener
{
	/**
	 *
	 */
	private static final long serialVersionUID = -1013647722305985723L;

	private Vbox mainBox = new Vbox();

	private Listbox lstLocator = new Listbox();
	private Listbox lstWarehouse = new Listbox();

	private Checkbox chkCreateNew = new Checkbox();

	private Textbox txtWarehouse = new Textbox();
	private Textbox txtAisleX = new Textbox();
	private Textbox txtBinY = new Textbox();
	private Textbox txtLevelZ = new Textbox();
	private Textbox txtKey = new Textbox();

	private Label lblLocator = new Label();
	private Label lblWarehouse = new Label();
	private Label lblAisleX = new Label();
	private Label lblBinY = new Label();
	private Label lblLevelZ = new Label();
	private Label lblKey = new Label();

	private Button btnCancel = new Button();
	private Button btnOk = new Button();

	private MLocatorLookup m_mLocator;

	private int m_WindowNo;
	private int m_M_Locator_ID;
	private int m_AD_Client_ID;
	private int m_AD_Org_ID;
	private int m_only_Warehouse_ID;
	private int m_M_Warehouse_ID;

	private boolean m_mandatory;

	private String m_M_WarehouseName;
	private String m_M_WarehouseValue;
	private String m_Separator;

	private boolean m_change;
	private boolean m_isCancel = true;

	private String title;

	private static CLogger log = CLogger.getCLogger(WLocatorDialog.class);

	public static interface LocatorDialogListener
	{
		public void onClose(Integer locatorId, boolean isChanged, boolean isCancel);
	}

	private LocatorDialogListener locatorDialogListener;

	public void setLocatorDialogListener(LocatorDialogListener listener)
	{
		this.locatorDialogListener = listener;
	}

	private void fireLocatorDialogClose()
	{
		if (locatorDialogListener != null)
		{
			locatorDialogListener.onClose(getValue(), m_change, m_isCancel);
		}
	}

	/**
	 *	Constructor
	 *  @param title title
	 *  @param mLocator locator
	 *  @param M_Locator_ID locator id
	 * 	@param mandatory mandatory
	 * 	@param only_Warehouse_ID if not 0 restrict warehouse
	 *  @param windowNo window no
	 */
	public WLocatorDialog(String title, MLocatorLookup mLocator,
			int M_Locator_ID, boolean mandatory, int only_Warehouse_ID,
			int windowNo)
	{
		super();

		m_WindowNo = windowNo;
		this.title = title;
		initComponents();

		m_mLocator = mLocator;
		m_M_Locator_ID = M_Locator_ID;
		m_mandatory = mandatory;
		m_only_Warehouse_ID = only_Warehouse_ID;

		initLocator();
	}

	private void initComponents()
	{
	    lblLocator.setValue(Msg.translate(Env.getCtx(), "M_Locator_ID"));
	    lblWarehouse.setValue(Msg.translate(Env.getCtx(), "M_Warehouse_ID"));
	    lblAisleX.setValue(Msg.getElement(Env.getCtx(), "X"));
	    lblBinY.setValue(Msg.getElement(Env.getCtx(), "Y"));
	    lblLevelZ.setValue(Msg.getElement(Env.getCtx(), "Z"));
	    lblKey.setValue(Msg.translate(Env.getCtx(), "Value"));

	    Hbox boxLocator = new Hbox();
	    boxLocator.setWidth("100%");

	    lblLocator.setWidth("30%");
	    lstLocator.setWidth("70%");
	    lstLocator.setMold("select");

	    boxLocator.appendChild(lblLocator);
	    boxLocator.appendChild(lstLocator);

	    Hbox boxCheckbox = new Hbox();
	    boxCheckbox.setWidth("100%");
	    boxCheckbox.setStyle("text-align: left;");

	    Label checkboxSpacer = new Label();
	    checkboxSpacer.setWidth("30%");
	    chkCreateNew.setWidth("70%");
	    chkCreateNew.setLabel(Msg.getMsg(Env.getCtx(), "CreateNew"));

	    boxCheckbox.appendChild(checkboxSpacer);
	    boxCheckbox.appendChild(chkCreateNew);

	    Hbox boxWarehouse = new Hbox();
	    boxWarehouse.setWidth("100%");

	    lblWarehouse.setWidth("30%");
	    lstWarehouse.setWidth("70%");
	    txtWarehouse.setWidth("70%");

	    lstWarehouse.setMold("select");


	    boxWarehouse.appendChild(lblWarehouse);
	    boxWarehouse.appendChild(lstWarehouse);
	    boxWarehouse.appendChild(txtWarehouse);

	    Hbox boxAisle = new Hbox();
	    boxAisle.setWidth("100%");

	    lblAisleX.setWidth("30%");
	    txtAisleX.setWidth("70%");

	    boxAisle.appendChild(lblAisleX);
	    boxAisle.appendChild(txtAisleX);

	    Hbox boxBin = new Hbox();
	    boxBin.setWidth("100%");

	    lblBinY.setWidth("30%");
	    txtBinY.setWidth("70%");

	    boxBin.appendChild(lblBinY);
	    boxBin.appendChild(txtBinY);

	    Hbox boxLevel = new Hbox();
	    boxLevel.setWidth("100%");

	    lblLevelZ.setWidth("30%");
	    txtLevelZ.setWidth("70%");

	    boxLevel.appendChild(lblLevelZ);
	    boxLevel.appendChild(txtLevelZ);

	    Hbox boxKey = new Hbox();
	    boxKey.setWidth("100%");

	    lblKey.setWidth("30%");
	    txtKey.setWidth("70%");

	    boxKey.appendChild(lblKey);
	    boxKey.appendChild(txtKey);

	    Hbox boxButtons = new Hbox();
	    boxButtons.setWidth("100%");
	    boxButtons.setPack("end");
	    boxButtons.setSpacing("4px");
	    boxButtons.setStyle(
	            "padding: 6px;" +
	            "box-sizing: border-box;"
	    );

	    btnCancel.setImage("/images/Cancel16.png");
	    btnCancel.setWidth("34px");
	    btnCancel.setHeight("30px");
	    btnCancel.setTooltiptext(Msg.getMsg(Env.getCtx(), "Cancel"));
	    btnCancel.setStyle(
	            "padding: 5px;" +
	            "min-width: 34px;" +
	            "box-sizing: border-box;"
	    );
	    btnCancel.addEventListener(Events.ON_CLICK, this);

	    btnOk.setImage("/images/Ok16.png");
	    btnOk.setWidth("34px");
	    btnOk.setHeight("30px");
	    btnOk.setTooltiptext(Msg.getMsg(Env.getCtx(), "OK"));
	    btnOk.setStyle(
	            "padding: 5px;" +
	            "min-width: 34px;" +
	            "box-sizing: border-box;"
	    );
	    btnOk.addEventListener(Events.ON_CLICK, this);

	    boxButtons.appendChild(btnCancel);
	    boxButtons.appendChild(btnOk);

	    mainBox.setWidth("250px");
	    mainBox.setStyle("text-align: right;");
	    mainBox.appendChild(boxLocator);
	    mainBox.appendChild(new Separator());
	    mainBox.appendChild(boxCheckbox);
	    mainBox.appendChild(new Separator());
	    mainBox.appendChild(boxWarehouse);
	    mainBox.appendChild(boxAisle);
	    mainBox.appendChild(boxBin);
	    mainBox.appendChild(boxLevel);
	    mainBox.appendChild(boxKey);
	    mainBox.appendChild(new Separator());
	    mainBox.appendChild(boxButtons);

	    this.appendChild(mainBox);
	    this.setTitle(title);
	    this.setClosable(true);
	    this.setBorder("normal");
	    this.setWidth("260px");
	    this.setAttribute("mode", "modal");
	    this.setSizable(true);
	}

	private void initLocator()
	{
		log.fine("");

		String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse";

		if (m_only_Warehouse_ID != 0)
			sql += " WHERE M_Warehouse_ID=" + m_only_Warehouse_ID;

		String SQL = MRole.getDefault().addAccessSQL(
				sql, "M_Warehouse", MRole.SQL_NOTQUALIFIED, MRole.SQL_RO)
				+ " ORDER BY 2";

		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL, null);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next())
			{
				KeyNamePair key = new KeyNamePair(rs.getInt(1), rs.getString(2));
				lstWarehouse.appendItem(key.getName(), key);
			}

			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, SQL, e);
		}

		log.fine("Warehouses=" + lstWarehouse.getItemCount());

		m_mLocator.fillComboBox(m_mandatory, true, true, false);

		log.fine(m_mLocator.toString());

		for (int i = 0; i < m_mLocator.getSize(); i++)
		{
			Object obj = m_mLocator.getElementAt(i);
			lstLocator.appendItem(obj.toString(), obj);
		}

		if (lstLocator.getItemCount() > 0)
			lstLocator.setSelectedIndex(0);

		lstLocator.addEventListener(Events.ON_SELECT, this);

		displayLocator();

		chkCreateNew.setChecked(false);
		chkCreateNew.addEventListener(Events.ON_CHECK, this);

		enableNew();

		lstWarehouse.addEventListener(Events.ON_SELECT, this);

		txtAisleX.addEventListener(Events.ON_CHANGE, this);
		txtBinY.addEventListener(Events.ON_CHANGE, this);
		txtLevelZ.addEventListener(Events.ON_CHANGE, this);
	}

	private void displayLocator()
	{
		MLocator l = null;
		ListItem listitem = lstLocator.getSelectedItem();

		if (listitem != null)
			l = (MLocator) listitem.getValue();

		if (l == null)
			return;

		m_M_Locator_ID = l.getM_Locator_ID();

		txtWarehouse.setText(l.getWarehouseName());
		txtAisleX.setText(l.getX());
		txtBinY.setText(l.getY());
		txtLevelZ.setText(l.getZ());
		txtKey.setText(l.getValue());

		getWarehouseInfo(l.getM_Warehouse_ID());

		int size = lstWarehouse.getItemCount();

		for (int i = 0; i < size; i++)
		{
			ListItem listItem = lstWarehouse.getItemAtIndex(i);
			KeyNamePair pp = (KeyNamePair) listItem.getValue();

			if (pp.getKey() == l.getM_Warehouse_ID())
			{
				lstWarehouse.setSelectedIndex(i);
				break;
			}
		}
	}

	private void enableNew()
	{
		boolean sel = chkCreateNew.isChecked();

		lstWarehouse.setVisible(sel);
		txtWarehouse.setVisible(!sel);

		txtWarehouse.setReadonly(true);
		txtAisleX.setReadonly(!sel);
		txtBinY.setReadonly(!sel);
		txtLevelZ.setReadonly(!sel);
		txtKey.setReadonly(!sel);
	}

	private void getWarehouseInfo(int M_Warehouse_ID)
	{
		if (M_Warehouse_ID == m_M_Warehouse_ID)
			return;

		m_M_Warehouse_ID = 0;
		m_M_WarehouseName = "";
		m_M_WarehouseValue = "";
		m_Separator = ".";
		m_AD_Client_ID = 0;
		m_AD_Org_ID = 0;

		String SQL = "SELECT M_Warehouse_ID, Value, Name, Separator, AD_Client_ID, AD_Org_ID "
				+ "FROM M_Warehouse WHERE IsActive = 'Y' AND M_Warehouse_ID=?";

		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL, null);
			pstmt.setInt(1, M_Warehouse_ID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next())
			{
				m_M_Warehouse_ID = rs.getInt(1);
				m_M_WarehouseValue = rs.getString(2);
				m_M_WarehouseName = rs.getString(3);
				m_Separator = rs.getString(4);
				m_AD_Client_ID = rs.getInt(5);
				m_AD_Org_ID = rs.getInt(6);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, SQL, e);
		}
	}

	private void createValue()
	{
		ListItem listitem = lstWarehouse.getSelectedItem();
		if (listitem == null)
			return;

		KeyNamePair pp = (KeyNamePair) listitem.getValue();
		if (pp == null)
			return;

		getWarehouseInfo(pp.getKey());

		StringBuffer buf = new StringBuffer(m_M_WarehouseValue);
		buf.append(m_Separator).append(txtAisleX.getText());
		buf.append(m_Separator).append(txtBinY.getText());
		buf.append(m_Separator).append(txtLevelZ.getText());

		txtKey.setText(buf.toString());
	}

	private boolean actionOK()
	{
		if (chkCreateNew.isChecked())
		{
			ListItem listitem = lstWarehouse.getSelectedItem();
			KeyNamePair pp = listitem != null ? (KeyNamePair) listitem.getValue() : null;

			if (pp != null)
				getWarehouseInfo(pp.getKey());

			String mandatoryFields = "";

			if (m_M_Warehouse_ID == 0)
				mandatoryFields += lblWarehouse.getValue() + " - ";

			if (txtKey.getText().length() == 0)
				mandatoryFields += lblKey.getValue() + " - ";

			if (txtAisleX.getText().length() == 0)
				mandatoryFields += lblAisleX.getValue() + " - ";

			if (txtBinY.getText().length() == 0)
				mandatoryFields += lblBinY.getValue() + " - ";

			if (txtLevelZ.getText().length() == 0)
				mandatoryFields += lblLevelZ.getValue() + " - ";

			if (mandatoryFields.length() != 0)
			{
				FDialog.error(m_WindowNo, this, "FillMandatory",
						mandatoryFields.substring(0, mandatoryFields.length() - 3));
				return false;
			}

			MLocator loc = MLocator.get(Env.getCtx(), m_M_Warehouse_ID, txtKey.getText(),
					txtAisleX.getText(), txtBinY.getText(), txtLevelZ.getText());

			if (loc == null || loc.getM_Locator_ID() <= 0)
			{
				FDialog.error(m_WindowNo, this, "Error");
				return false;
			}

			m_M_Locator_ID = loc.getM_Locator_ID();

			lstLocator.appendItem(loc.toString(), loc);
			lstLocator.setSelectedIndex(lstLocator.getItemCount() - 1);
		}
		else
		{
			ListItem listitem = lstLocator.getSelectedItem();
			MLocator l = listitem != null ? (MLocator) listitem.getValue() : null;

			if (l == null || l.getM_Locator_ID() <= 0)
			{
				if (m_mandatory)
				{
					FDialog.error(m_WindowNo, this, "FillMandatory", lblLocator.getValue());
					return false;
				}

				m_M_Locator_ID = 0;
			}
			else
			{
				m_M_Locator_ID = l.getM_Locator_ID();
			}
		}

		log.warning("WLocatorDialog.actionOK - M_Locator_ID=" + m_M_Locator_ID);
		return true;
	}

	public Integer getValue()
	{
		ListItem listitem = lstLocator.getSelectedItem();
		MLocator l = listitem != null ? (MLocator) listitem.getValue() : null;

		if (l != null && l.getM_Locator_ID() != 0)
			return Integer.valueOf(l.getM_Locator_ID());

		if (m_M_Locator_ID > 0)
			return Integer.valueOf(m_M_Locator_ID);

		return null;
	}

	public boolean isChanged()
	{
		return m_change;
	}

	@Override
	public void onClose()
	{
		if (!m_change)
			m_isCancel = true;

		fireLocatorDialogClose();
		super.onClose();
	}

	public void onEvent(Event event) throws Exception
	{
		if (event == null)
			return;

		if (event.getTarget() == btnCancel)
		{
			m_change = false;
			m_isCancel = true;
			fireLocatorDialogClose();
			this.detach();
		}
		else if (event.getTarget() == btnOk)
		{
			if (!actionOK())
				return;

			m_change = true;
			m_isCancel = false;
			fireLocatorDialogClose();
			this.detach();
		}
		else if (event.getTarget() == lstLocator)
		{
			displayLocator();
		}
		else if (event.getTarget() == chkCreateNew)
		{
			enableNew();
		}
		else if (chkCreateNew.isChecked() && event.getTarget() == lstWarehouse)
		{
			createValue();
		}
	}
}