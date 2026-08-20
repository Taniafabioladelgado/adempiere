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

package org.adempiere.webui.window;

import java.util.Properties;

import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trace;

import org.zkoss.zk.ui.Component;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Messagebox;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<li> Cancel action does not apply when delete a record
 *		@see https://adempiere.atlassian.net/browse/ADEMPIERE-451
 * @date    Feb 25, 2007
 * @version $Revision: 0.10 $
 */

public class FDialog
{
	/**	Logger			*/
    private static final CLogger logger = CLogger.getCLogger(FDialog.class);

    public interface AskCallback
    {
    	void onAnswer(boolean ok);
    }

    /**
     * Construye el mensaje traducido y agrega el mensaje adicional
     * utilizando saltos de línea compatibles con ZK 10.
     *
     * @param adMessage mensaje registrado en AD_Message
     * @param message mensaje adicional
     * @return mensaje completo con saltos de línea
     */
    private static String constructMessage(
            String adMessage,
            String message) {

        StringBuilder out = new StringBuilder();

        if (adMessage != null && !adMessage.trim().isEmpty()) {
            out.append(Msg.getMsg(Env.getCtx(), adMessage));
        }

        if (message != null && !message.trim().isEmpty()) {

            if (out.length() > 0) {
                out.append("\n\n");
            }

            out.append(message);
        }

        return out.toString()
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

	/**
	 *	Display warning with warning icon
	 *
	 *	@param	windowNo	Number of Window
	 *	@param	adMessage	Message to be translated
	 *	@param	title		Message box title
	 *
	 * @see #warn(int, String)
	 * @see #warn(int, Component, String, String, String)
	 * @see #warn(int, Component, String, String)
	 */
    public static void warn(int windowNo, String adMessage, String title)
    {
        warn(windowNo, null, adMessage, null, title);
    }

	/**
	 *	Display warning with warning icon
	 *	@param	windowNo	Number of Window
	 *	@param	adMessage	Message to be translated
	 *	@param	message		Additional message
	 *	@param	title		If none then one will be generated
	 *
	 * @see #warn(int, String)
	 * @see #warn(int, String, String)
	 * @see #warn(int, Component, String, String, String)
	 */
    public static void warn(int windowNo, Component comp, String adMessage, String message)
    {
    	warn(windowNo, comp, adMessage, message, null);
    }

	/**
	 *	Display warning with warning icon
	 *	@param	windowNo	Number of Window
	 *	@param	adMessage	Message to be translated
	 *	@param	message		Additional message
	 *	@param	title		If none then one will be generated
	 *
	 * @see #warn(int, String)
	 * @see #warn(int, String, String)
	 * @see #warn(int, Component, String, String)
	 */
    public static void warn(int windowNo, Component comp, String adMessage, String message, String title)
    {
    	Properties ctx = Env.getCtx();

    	logger.info(adMessage + " - " + message);

    	String newTitle;

    	if (title == null)
    	{
    		newTitle = AEnv.getDialogHeader(ctx, windowNo);
    	}
    	else
    	{
    		newTitle = title;
    	}

    	String out = constructMessage(adMessage, message);

		try
		{
			Messagebox.showDialog(out, newTitle, Messagebox.OK, Messagebox.EXCLAMATION);
		}
		catch (InterruptedException exception)
		{
			Thread.currentThread().interrupt();
		}

		return;
    }

	/**
	 *	Display warning with warning icon
	 *	@param	windowNo	Number of Window
	 *	@param	adMessage	Message to be translated
	 *
	 *	@see #warn(int, String, String)
	 *	@see #warn(int, Component, String, String, String)
	 * @see #warn(int, Component, String, String)
	 */
    public static void warn(int windowNo, String adMessage)
    {
        warn(windowNo, null, adMessage, null, null);
    }

	/**
	 *	Display error with error icon
	 *	@param	windowNo	Number of Window
	 *  @param	comp		Component (unused)
	 *	@param	adMessage	Message to be translated
	 */
    public static void error(int windowNo, Component comp, String adMessage)
    {
        error(windowNo, comp, adMessage, null);
    }

	/**
	 *	Display error with error icon
	 *	@param	windowNo	Number of Window
	 *	@param	adMessage	Message to be translated
	 *
	 *  @see #error(int, String, String)
	 *  @see #error(int, Component, String)
	 *  @see #error(int, Component, String, String)
	 */
	public static void error(int windowNo, String adMessage)
	{
		error(windowNo, null, adMessage, null);
	}	//	error (int, String)

	/**
	 *	Display error with error icon
	 *	@param	windowNo	Number of Window
	 *	@param	adMessage	Message to be translated
	 *	@param	adMessage	Additional message
	 *
	 *  @see #error(int, String)
	 *  @see #error(int, Component, String)
	 *  @see #error(int, Component, String, String)
	 */
    public static void error(int windowNo, String adMessage, String msg)
    {
        error(windowNo, null, adMessage, msg);
    }

	/**
	 *	Display error with error icon.
	 *
	 *	@param	windowNo	Number of Window
	 *  @param	comp		Component (unused)
	 *	@param	adMessage	Message to be translated
	 *	@param	message		Additional message
	 *
	 *  @see #error(int, String)
	 *  @see #error(int, Component, String)
	 *  @see #error(int, String, String)
	 */
    public static void error(int windowNo, Component comp, String adMessage, String message)
    {
    	Properties ctx = Env.getCtx();

		logger.info(adMessage + " - " + message);

		if (CLogMgt.isLevelFinest())
		{
			Trace.printStack();
		}

		String out = constructMessage(adMessage, message);

		try
		{
			Messagebox.showDialog(out, AEnv.getDialogHeader(ctx, windowNo), Messagebox.OK, Messagebox.ERROR);
		}
		catch (InterruptedException exception)
		{
            Thread.currentThread().interrupt();
		}

		return;
    }

    /**************************************************************************
	 *	Ask Question with question icon and (OK) (Cancel) buttons
	 *
	 *	@param	windowNo	Number of Window
	 *  @param  comp        Container (owner)
	 *	@param	adMessage	Message to be translated
	 *	@param	msg			Additional clear text message
	 *
	 *	@return true, if OK
	 */
    public static boolean ask(int windowNo, Component comp, String adMessage, String msg)
    {
    	String out = constructMessage(adMessage, msg);

        try
        {
            int response = Messagebox.showDialog(
            	out,
            	AEnv.getDialogHeader(Env.getCtx(), windowNo),
            	Messagebox.OK | Messagebox.CANCEL,
            	Messagebox.QUESTION
            );

            return (response == Messagebox.OK);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

		return true;
    }

    public static void ask(int windowNo, Component comp, String adMessage, String msg, final AskCallback callback)
    {
    	String out = constructMessage(adMessage, msg);

    	Messagebox.showDialog(
    		out,
    		AEnv.getDialogHeader(Env.getCtx(), windowNo),
    		Messagebox.OK | Messagebox.CANCEL,
    		Messagebox.QUESTION,
    		new Messagebox.ResultListener() {
				@Override
				public void onResult(int result)
				{
					if (callback != null)
					{
						callback.onAnswer(result == Messagebox.OK);
					}
				}
			}
    	);
    }

	/**************************************************************************
	 *	Ask Question with question icon and (OK) (Cancel) buttons
	 *
	 *	@param	windowNo	Number of Window
	 *  @param  comp        Container (owner)
	 *	@param	adMessage	Message to be translated
	 *
	 *	@return true, if OK
	 */
    public static boolean ask(int windowNo, Component comp, String adMessage)
    {
        return ask(windowNo, comp, adMessage, (String) null);
    }

    public static void ask(int windowNo, Component comp, String adMessage, final AskCallback callback)
    {
        ask(windowNo, comp, adMessage, (String) null, callback);
    }

    /**
     *  Display information with information icon.
     *
     *  @param  windowNo    Number of Window
     *  @param  comp        Component (unused)
     *  @param  adMessage   Message to be translated
     *
     *  @see #info(int, Component, String, String)
     */
    public static void info(int windowNo, Component comp, String adMessage)
    {
        info(windowNo, comp, adMessage, null);

        return;
    }

    /**
     *  Display information with information icon.
     *
     *  @param  windowNo    Number of Window
     *  @param  comp        Component (unused)
     *  @param  adMessage   Message to be translated
     *  @param  message     Additional message
     *
     *  @see #info(int, Component, String)
     */
    public static void info(int windowNo, Component comp, String adMessage, String message)
    {
        Properties ctx = Env.getCtx();

        logger.info(adMessage + " - " + message);

        if (CLogMgt.isLevelFinest())
        {
            Trace.printStack();
        }

        String out = constructMessage(adMessage, message).toString();

        try
        {
        	Messagebox.showDialog(out, AEnv.getDialogHeader(ctx, windowNo), Messagebox.OK, Messagebox.INFORMATION);
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
        }

        return;
    }
}