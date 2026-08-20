/******************************************************************************
 * Copyright (C) 2008 Low Heng Sin                                            *
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
package org.adempiere.webui.component;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MTree;
import org.compiere.model.MTreeNode;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.event.TreeDataEvent;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SimpleTreeModel extends DefaultTreeModel<MTreeNode> implements TreeitemRenderer<DefaultTreeNode<MTreeNode>>, EventListener {

    private static final long serialVersionUID = -4649471521757131755L;

    private static final CLogger logger = CLogger.getCLogger(SimpleTreeModel.class);

    private boolean itemDraggable;
    private List<EventListener> onDropListners = new ArrayList<EventListener>();

    public SimpleTreeModel(DefaultTreeNode<MTreeNode> root) {
        super(root);
    }
    public static SimpleTreeModel initADTree(Tree tree, int treeId, int windowNo, String whereClause) {
        return initADTree(tree, treeId, windowNo, true, whereClause, null);
    }

    public static SimpleTreeModel initADTree(Tree tree, int AD_Tree_ID, int windowNo, boolean editable, String whereClause, String trxName) {
        if (!Util.isEmpty(whereClause)) {
            whereClause = Env.parseContext(Env.getCtx(), windowNo, whereClause, false, false);
        }

        MTree vTree = new MTree(Env.getCtx(), AD_Tree_ID, editable, true, whereClause, trxName);
        MTreeNode root = vTree.getRoot();

        SimpleTreeModel treeModel = SimpleTreeModel.createFrom(root);
        treeModel.setItemDraggable(editable);

        if (editable) {
            treeModel.addOnDropEventListener(new ADTreeOnDropListener(tree, treeModel, vTree, windowNo));
        }

        if (tree.getTreecols() == null) {
            Treecols treeCols = new Treecols();
            tree.appendChild(treeCols);

            Treecol treeCol = new Treecol();
            treeCols.appendChild(treeCol);
        }

        tree.setPageSize(-1);

        try {
            tree.setItemRenderer(treeModel);
            tree.setModel(treeModel);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to setup tree", e);
        }

        return treeModel;
    }

    /**
     * Convierte el árbol de Eureka al modelo utilizado por ZK.
     *
     * Los nodos con hijos se crean como ramas y los nodos finales
     * se crean como hojas para evitar que ZK muestre una flecha
     * de expansión incorrecta.
     */
    public static SimpleTreeModel createFrom(MTreeNode root) {

        DefaultTreeNode stRoot =
            new DefaultTreeNode(root, new ArrayList());

        Enumeration<javax.swing.tree.TreeNode> nodeEnum =
            root.children();

        while (nodeEnum.hasMoreElements()) {

            MTreeNode childNode =
                (MTreeNode) nodeEnum.nextElement();

            DefaultTreeNode stNode;

            if (childNode.getChildCount() > 0) {

                // Nodo que realmente contiene hijos.
                stNode = new DefaultTreeNode(
                    childNode,
                    new ArrayList()
                );

                populate(stNode, childNode);

            } else {

                // Nodo final: no debe mostrar flecha de expansión.
                stNode = new DefaultTreeNode(childNode);
            }

            stRoot.getChildren().add(stNode);
        }

        return new SimpleTreeModel(stRoot);
    }

    /**
     * Agrega recursivamente los hijos de un nodo.
     */
    private static void populate(
            DefaultTreeNode stNode,
            MTreeNode root) {

        Enumeration<javax.swing.tree.TreeNode> nodeEnum =
            root.children();

        while (nodeEnum.hasMoreElements()) {

            MTreeNode childNode =
                (MTreeNode) nodeEnum.nextElement();

            DefaultTreeNode stChildNode;

            if (childNode.getChildCount() > 0) {

                // Rama expandible.
                stChildNode = new DefaultTreeNode(
                    childNode,
                    new ArrayList()
                );

                populate(stChildNode, childNode);

            } else {

                // Hoja final sin flecha.
                stChildNode = new DefaultTreeNode(childNode);
            }

            stNode.getChildren().add(stChildNode);
        }
    }

    @Override
    public void render(Treeitem ti, DefaultTreeNode<MTreeNode> node, int index) throws Exception {
        Treecell tc = new Treecell(Objects.toString(node.getData()));
        Treerow tr = null;

        if (ti.getTreerow() == null) {
            tr = new Treerow();
            tr.setParent(ti);

            if (isItemDraggable()) {
                tr.setDraggable("true");
            }

            if (!onDropListners.isEmpty()) {
                tr.setDroppable("true");
                tr.addEventListener(Events.ON_DROP, this);
            }
        } else {
            tr = ti.getTreerow();
            tr.getChildren().clear();
        }

        tc.setParent(tr);
        ti.setValue(node);
    }

    public void addNode(DefaultTreeNode newNode) {
        DefaultTreeNode root = getRoot();
        root.getChildren().add(newNode);

        int index = root.getChildCount() - 1;
        fireEvent(TreeDataEvent.INTERVAL_ADDED, getPath(root), index, index);
    }

    @Override
    public DefaultTreeNode getRoot() {
        return (DefaultTreeNode) super.getRoot();
    }
    @Override
    public DefaultTreeNode getChild(org.zkoss.zul.TreeNode parent, int index) {
        return (DefaultTreeNode) super.getChild(parent, index);
    }

    public void removeNode(DefaultTreeNode treeNode) {
        int path[] = this.getPath(treeNode);

        if (path != null && path.length > 0) {
            DefaultTreeNode parentNode = getRoot();
            int index = path.length - 1;

            for (int i = 0; i < index; i++) {
                parentNode = getChild(parentNode, path[i]);
            }

            int removeIndex = path[index];
            parentNode.getChildren().remove(removeIndex);

            fireEvent(TreeDataEvent.INTERVAL_REMOVED, getPath(parentNode), removeIndex, removeIndex);
        }
    }

    public void setItemDraggable(boolean b) {
        itemDraggable = b;
    }

    public boolean isItemDraggable() {
        return itemDraggable;
    }

    public void addOnDropEventListener(EventListener listener) {
        onDropListners.add(listener);
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if (Events.ON_DROP.equals(event.getName())) {
            for (EventListener listener : onDropListners) {
                listener.onEvent(event);
            }
        }
    }

    public DefaultTreeNode getParent(DefaultTreeNode treeNode) {
        int path[] = this.getPath(treeNode);

        if (path != null && path.length > 0) {
            DefaultTreeNode parentNode = getRoot();
            int index = path.length - 1;

            for (int i = 0; i < index; i++) {
                parentNode = getChild(parentNode, path[i]);
            }

            return parentNode;
        }

        return null;
    }

    public void addNode(DefaultTreeNode newParent, DefaultTreeNode newNode, int index) {
        newParent.getChildren().add(index, newNode);
        fireEvent(TreeDataEvent.INTERVAL_ADDED, getPath(newParent), index, index);
    }

    public DefaultTreeNode find(DefaultTreeNode fromNode, int recordId) {
        if (fromNode == null) {
            fromNode = getRoot();
        }

        MTreeNode data = (MTreeNode) fromNode.getData();

        if (data.getNode_ID() == recordId) {
            return fromNode;
        }

        try {
            if (isLeaf(fromNode)) {
                return null;
            }
        } catch (NullPointerException e) {
            logger.severe("Uninitialized node exists in tree. Node ID: " + data.getNode_ID());
            return null;
        }

        int cnt = getChildCount(fromNode);

        for (int i = 0; i < cnt; i++) {
            DefaultTreeNode child = getChild(fromNode, i);
            DefaultTreeNode treeNode = find(child, recordId);

            if (treeNode != null) {
                return treeNode;
            }
        }

        return null;
    }

    public void nodeUpdated(DefaultTreeNode node) {
        DefaultTreeNode parent = getParent(node);

        if (parent != null) {
            int index = parent.getChildren().indexOf(node);
            fireEvent(TreeDataEvent.CONTENTS_CHANGED, getPath(parent), index, index);
        }
    }
}
