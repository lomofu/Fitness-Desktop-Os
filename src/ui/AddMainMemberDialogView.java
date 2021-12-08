package ui;

import component.MemberTable;
import data.DataSourceHandler;

import javax.swing.*;
import java.awt.*;

import static constant.UIConstant.MEMBER_COLUMNS_SELECTED_MODE;
import static constant.UIConstant.MEMBER_COLUMNS_SELECTED_MODE_FILTER_COLUMNS;

/**
 * @author lomofu
 * @desc
 * @create 08/Dec/2021 03:33
 */
public class AddMainMemberDialogView extends JDialog {
  public AddMainMemberDialogView(Frame owner, AddMemberDialogView addMemberDialogView) {
    initDialog(owner);
    MemberTable memberTable = initMemberTable(addMemberDialogView);
    Box verticalBox = Box.createVerticalBox();
    verticalBox.add(memberTable.getTitle());
    verticalBox.add(memberTable.getjToolBar());
    verticalBox.add(Box.createVerticalStrut(10));
    verticalBox.add(memberTable.getFilterBar());

    this.add(verticalBox, BorderLayout.NORTH);
    this.add(memberTable.getjScrollPane(), BorderLayout.CENTER);
  }

  public static void showDig(Frame owner, AddMemberDialogView addMemberDialogView) {
    AddMainMemberDialogView dialog = new AddMainMemberDialogView(owner, addMemberDialogView);
    dialog.setVisible(true);
  }

  private MemberTable initMemberTable(AddMemberDialogView addMemberDialogView) {
    String parentId = addMemberDialogView.getParentId();
    return new MemberTable(
        addMemberDialogView,
        this,
        "Membership List",
        MEMBER_COLUMNS_SELECTED_MODE,
        DataSourceHandler.findMembersForMainTableRender(parentId),
        MEMBER_COLUMNS_SELECTED_MODE_FILTER_COLUMNS,
        true,
        3);
  }

  private void initDialog(Frame owner) {
    this.setTitle("Add Main Member");
    this.setSize(800, 600);
    this.setPreferredSize(new Dimension(800, 600));
    this.setResizable(true);
    this.setLocationRelativeTo(owner);
  }
}
