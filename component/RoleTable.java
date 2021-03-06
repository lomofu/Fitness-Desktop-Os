package component;

import bean.DataSourceChannelInfo;
import constant.DataManipulateEnum;
import constant.UIConstant;
import core.RoleService;
import data.DataSource;
import data.DataSourceChannel;
import dto.RoleDto;
import ui.CheckRoleDialogView;
import ui.ClubFrameView;
import ui.RoleDialogView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * @author lomofu
 * <p>
 * This class sets the role table and implement related functions
 * <p>
 * extends@MyTable: extends abstract table function
 * implements@DataSourceChannel: let this object be observer to observe the data source change
 * and call back the override onchange functions(mainly update the UI).
 * Meanwhile, it will subscrib the data source when table init
 */
public class RoleTable extends MyTable implements DataSourceChannel<RoleDto> {
    // default table model have full functions
    public RoleTable(
            ClubFrameView clubFrameView,
            String title,
            String[] columns,
            Object[][] data,
            int[] filterColumns) {
        super(clubFrameView, title, columns, data, filterColumns);
        this.subscribe(RoleDto.class);
        initMyEvents();
    }

    /**
     * This method adds some buttons into the toolbar
     */
    @Override
    protected void addComponentsToToolBar() {
        JButton addRoleBtn =
                new TableToolButton(UIConstant.TABLE_TOOL_LIST[0][0], MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[0][1]));
        addRoleBtn.addActionListener(e -> RoleDialogView.showDig(clubFrameView));

        JButton editRoleBtn =
                new TableToolButton(UIConstant.TABLE_TOOL_LIST[1][0], MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[1][1]));
        editRoleBtn.setEnabled(false);
        editRoleBtn.addActionListener(e -> RoleDialogView.showDig(clubFrameView, (String) jTable.getModel().getValueAt(jTable.convertRowIndexToModel(jTable.getSelectedRow()), 0)));

        JButton refreshBtn = new TableToolButton(UIConstant.TABLE_TOOL_LIST[6][0], MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[6][1]));
        refreshBtn.addActionListener(__ -> {
            Rectangle rect = jTable.getCellRect(0, 0, true);
            jTable.scrollRectToVisible(rect);
            editRoleBtn.setEnabled(false);
            fetchData();
        });

        JButton filterBtn = new TableToolButton("", MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[3][1]));
        filterBtn.setToolTipText(UIConstant.TABLE_TOOL_LIST[3][0]);
        filterBtn.addActionListener(e -> {
            if (this.filterBar.isVisible()) {
                filterBtn.setIcon(MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[3][1]));
                filterBtn.setToolTipText(UIConstant.TABLE_TOOL_LIST[3][0]);
                this.filterBar.setVisible(false);
                return;
            }

            filterBtn.setIcon(MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[4][1]));
            filterBtn.setToolTipText(UIConstant.TABLE_TOOL_LIST[4][0]);
            this.filterBar.setVisible(true);
        });

        this.jToolBar.add(addRoleBtn);
        this.jToolBar.addSeparator(new Dimension(10, 0));
        this.jToolBar.add(editRoleBtn);
        this.jToolBar.addSeparator(new Dimension(10, 0));
        this.jToolBar.add(refreshBtn);
        this.jToolBar.add(Box.createHorizontalGlue());
        this.jToolBar.add(this.searchBox);
    }

    /**
     * This method sets remove events, edit events and help events
     */
    private void initMyEvents() {
        this.jTable.getSelectionModel().addListSelectionListener(e -> {
            Component[] jToolBarComponents = jToolBar.getComponents();
            Component editBtn = jToolBarComponents[2];
            Component removeBtn = jToolBarComponents[4];

            // if more than one role are selected, the remove button is available and edit button is unavailable
            if (jTable.getSelectedRowCount() > 0) {
                editBtn.setEnabled(false);
                removeBtn.setEnabled(true);
            }
            // if only select one role, all button are available
            if (jTable.getSelectedRowCount() == 1) {
                editBtn.setEnabled(true);
            }
        });
        // Click the help button to view the guidance info
        this.helpBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this.jScrollPane, UIConstant.HELP_INFO[2], "Help", JOptionPane.QUESTION_MESSAGE, MyImageIcon.build(UIConstant.TABLE_TOOL_LIST[9][1])));
    }

    @Override
    protected void addComponentsToFilterBar() {
        // do nothing
    }

    /**
     * This method is used to monitor right mouse click events
     *
     * @param e mouse event
     */
    @Override
    protected void onRightClick(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        int[] selectedRows = table.getSelectedRows();

        // If the mouse is right-clicked without a row selected, no action is taken
        if (selectedRows.length == 0) {
            return;
        }

        Point point = e.getPoint();
        int row = table.rowAtPoint(point);

        // If no right mouse click is made on the selected row, no action is taken.
        if (Arrays.stream(selectedRows).filter(v -> v == row).findAny().isEmpty()) {
            return;
        }

        // If the right mouse click is made with individual rows selected, the menu option is displayed
        if (selectedRows.length == 1) {
            table.setRowSelectionInterval(row, row);
            TablePopMenu popMenu = getSingleTablePopMenu();
            popMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    // create a select menu with edit and view info option
    private TablePopMenu getSingleTablePopMenu() {
        return new TablePopMenu() {
            @Override
            void create() {
                JMenuItem check = new JMenuItem("Role Info");
                JMenuItem edit = new JMenuItem("Edit");

                check.addActionListener(__ -> CheckRoleDialogView.showDig(clubFrameView, (String) jTable.getModel().getValueAt(jTable.convertRowIndexToModel(jTable.getSelectedRow()), 0)));
                edit.addActionListener(__ -> RoleDialogView.showDig(clubFrameView, (String) jTable.getModel().getValueAt(jTable.convertRowIndexToModel(jTable.getSelectedRow()), 0)));
                this.add(check);
                this.add(edit);
            }
        };
    }

    // create a role info dialog when double-click the mouse on the selected row
    @Override
    protected void onDoubleClick(MouseEvent e) {
        CheckRoleDialogView.showDig(clubFrameView, (String) jTable.getModel().getValueAt(jTable.convertRowIndexToModel(jTable.getSelectedRow()), 0));
    }

    /**
     * This method override the observer hook function if the corresponding data in
     * data source has mutable.
     *
     * @param roleDto parameter from data source of
     *                which role object has been changed(only have value if is a update operation)
     * @param flag    operation type
     */
    @Override
    public void onDataChange(RoleDto roleDto, DataManipulateEnum flag) {
        switch (flag) {
            case INSERT -> insert();
            case UPDATE, DELETE -> SwingUtilities.invokeLater(this::fetchData);
        }
    }

    /**
     * This method refresh the data from data source
     */
    private void fetchData() {
        Component[] jToolBarComponents = jToolBar.getComponents();
        Component editBtn = jToolBarComponents[2];
        editBtn.setEnabled(false);

        DefaultTableModel model = (DefaultTableModel) this.jTable.getModel();
        model.setDataVector(RoleService.findRoles(), UIConstant.ROLE_COLUMNS);
        model.fireTableDataChanged();
        this.jTable.setModel(model);
        super.setTableStyle();
    }

    /**
     * This method is a call back for the insert action
     */
    private void insert() {
        SwingUtilities.invokeLater(() -> {
            fetchData();
            Rectangle rect = jTable.getCellRect(0, 0, true);
            jTable.scrollRectToVisible(rect);
        });
    }

    /**
     * This method subscribe data source when the object is init
     *
     * @param roleDtoClass roleDto.class
     */
    @Override
    public void subscribe(Class<RoleDto> roleDtoClass) {
        DataSource.subscribe(new DataSourceChannelInfo<>(this, roleDtoClass));
    }
}
