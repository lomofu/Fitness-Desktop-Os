package component;

import bean.Customer;
import ui.ClubFrameView;
import ui.EditMemberDialogView;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lomofu
 * @desc
 * @create 26/Nov/2021 00:29
 */
public abstract class MyTable {
  protected final DefaultTableModel tableModel = new DefaultTableModel();
  protected ClubFrameView clubFrameView;
  protected JScrollPane jScrollPane;
  protected JToolBar jToolBar;
  protected JTable jTable;
  protected JTextField searchTextField;
  protected String[] columns;
  protected String[][] data;
  protected Box searchBox;
  protected int[] filterColumns;
  protected JToolBar filterBar;

  public MyTable(
      ClubFrameView clubFrameView, String[] columns, String[][] data, int[] filterColumns) {
    this.clubFrameView = clubFrameView;
    this.columns = columns;
    this.data = data;
    this.filterColumns = filterColumns;

    initToolBar();
    initFilterBar();
    initTable();
    initScrollPane();
    initListeners();
  }

  public JScrollPane getjScrollPane() {
    return jScrollPane;
  }

  public JToolBar getjToolBar() {
    return jToolBar;
  }

  public JToolBar getFilterBar() {
    return filterBar;
  }

  private void initToolBar() {
    this.jToolBar = new JToolBar();
    this.jToolBar.setLayout(new BoxLayout(this.jToolBar, BoxLayout.X_AXIS));
    this.jToolBar.setFloatable(false);
    this.jToolBar.setPreferredSize(new Dimension(0, 40));

    searchBox = Box.createHorizontalBox();
    searchTextField = new JTextField(20);
    searchTextField.setMaximumSize(new Dimension(100, 40));
    searchBox.add(new JLabel("Search"));
    searchBox.add(Box.createHorizontalStrut(10));
    searchBox.add(searchTextField);

    addComponentsToToolBar();
  }

  private void initFilterBar() {
    this.filterBar = new JToolBar();
    this.filterBar.setLayout(new BoxLayout(this.filterBar, BoxLayout.Y_AXIS));
    this.filterBar.setFloatable(false);
    this.filterBar.setPreferredSize(null);
    this.filterBar.setBorder(BorderFactory.createEmptyBorder(10,5,10,0));
    this.filterBar.setVisible(false);

    addComponentsToFilterBar();
  }

  protected abstract void addComponentsToToolBar();

  protected abstract void addComponentsToFilterBar();

  private void initTable() {
    bindData();
    setTableStyle();
  }

  private void setTableStyle() {
    this.jTable.setGridColor(new Color(227, 227, 227));
    this.jTable.setShowHorizontalLines(false);
    this.jTable.setRowHeight(30);
    this.jTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

    JTableHeader tableHeader = jTable.getTableHeader();
    tableHeader.setFont(new Font(null, Font.PLAIN, 14));

    DefaultTableCellRenderer defaultTableHeaderRenderer =
        (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();
    defaultTableHeaderRenderer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
    defaultTableHeaderRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    Arrays.stream(columns)
        .forEachOrdered(
            e ->
                jTable
                    .getColumn(e)
                    .setCellRenderer(
                        new MyTableCellRender(this.searchTextField, this.filterColumns)));
  }

  private void bindData() {
    tableModel.setDataVector(data, columns);
    this.jTable =
        new JTable(this.tableModel) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
  }

  private void initScrollPane() {
    this.jScrollPane = new JScrollPane(jTable);
  }

  private void initListeners() {
    this.jTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
              onRightClick(e);
            }
          }

          @Override
          public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
              onDoubleClick(e);
            }
          }
        });

    searchTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                update();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                update();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                update();
              }

              private void update() {
                TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable.getModel());
                String text = searchTextField.getText();
                if (text.trim().length() == 0) {
                  jTable.setRowSorter(null);
                  return;
                }
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, filterColumns));
                jTable.setRowSorter(sorter);
              }
            });
  }

  protected abstract void onRightClick(MouseEvent e);

  protected abstract void onDoubleClick(MouseEvent e);

  private static class MyTableCellRender extends DefaultTableCellRenderer {
    private final JTextField searchField;
    private final int[] highlightColumns;

    public MyTableCellRender(JTextField searchField, int[] highlightColumns) {
      this.searchField = searchField;
      this.highlightColumns = highlightColumns;
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (row % 2 == 0) setBackground(new Color(213, 213, 213));
      else if (row % 2 == 1) setBackground(Color.white);
      Component c =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      JLabel original = (JLabel) c;
      original.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
      if (Arrays.stream(highlightColumns).filter(e -> e == column).findAny().isPresent()) {
        LabelHighlighted label = new LabelHighlighted();
        label.setFont(original.getFont());
        label.setText(original.getText());
        label.setBackground(original.getBackground());
        label.setForeground(original.getForeground());
        label.setHorizontalTextPosition(original.getHorizontalTextPosition());
        label.highlightText(searchField.getText());
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        return label;
      }
      return c;
    }
  }

  protected static class TableToolButton extends JButton implements MouseListener {
    public TableToolButton(String label, ImageIcon imageIcon) {
      super(label, imageIcon);
      this.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
      this.setBorderPainted(false);
      this.setIconTextGap(10);
      this.setCursor(new Cursor(Cursor.HAND_CURSOR));
      this.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
      JButton btn = (JButton) e.getComponent();
      btn.setBorderPainted(true);
      btn.setRolloverEnabled(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JButton btn = (JButton) e.getComponent();
      btn.setBorderPainted(false);
    }
  }

  protected static class TablePopMenu extends JPopupMenu {
    public TablePopMenu(Frame owner, Customer data) {
      JMenuItem edit = new JMenuItem("Edit");
      JMenuItem remove = new JMenuItem("Remove");

      edit.addActionListener(e -> EditMemberDialogView.showDig(owner, data));

      this.add(edit);
      this.add(remove);
    }

    public TablePopMenu(Frame owner, int[] rows) {
      JMenuItem remove = new JMenuItem("Remove");
      this.add(remove);
    }
  }

  private static class LabelHighlighted extends JLabel {
    private final List<Rectangle2D> rectangles = new ArrayList<>();
    private final Color colorHighlight = Color.YELLOW;

    public void reset() {
      rectangles.clear();
      repaint();
    }

    public void highlightText(String textToHighlight) {
      if (textToHighlight == null) {
        return;
      }
      reset();

      final String textToMatch = textToHighlight.toLowerCase().trim();
      if (textToMatch.length() == 0) {
        return;
      }
      textToHighlight = textToHighlight.trim();

      final String labelText = getText().toLowerCase();
      if (labelText.contains(textToMatch)) {
        FontMetrics fm = getFontMetrics(getFont());
        float w = -1;
        final float h = fm.getHeight() - 1;
        int i = 0;
        while (true) {
          i = labelText.indexOf(textToMatch, i);
          if (i == -1) {
            break;
          }
          if (w == -1) {
            String matchingText = getText().substring(i, i + textToHighlight.length());
            w = fm.stringWidth(matchingText);
          }
          String preText = getText().substring(0, i);
          float x = fm.stringWidth(preText);
          rectangles.add(new Rectangle2D.Float(x + 10, 8, w, h));
          i = i + textToMatch.length();
        }
        repaint();
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      if (rectangles.size() > 0) {
        Graphics2D g2d = (Graphics2D) g;
        Color c = g2d.getColor();
        for (Rectangle2D rectangle : rectangles) {
          g2d.setColor(colorHighlight);
          g2d.fill(rectangle);
          g2d.setColor(Color.LIGHT_GRAY);
          g2d.draw(rectangle);
        }
        g2d.setColor(c);
      }
      super.paintComponent(g);
    }
  }
}