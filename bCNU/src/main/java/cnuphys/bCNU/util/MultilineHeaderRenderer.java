/**
 *
 */
package cnuphys.bCNU.util;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * A Renderer for multiline table header labels to save space when the label is
 * bigger than the data being displayed
 *
 * @version 1.0 11/09/98
 */
@SuppressWarnings("serial")
public class MultilineHeaderRenderer extends JList implements TableCellRenderer {

	public MultilineHeaderRenderer() {
		setOpaque(true);
		setForeground(UIManager.getColor("TableHeader.foreground"));
		setBackground(UIManager.getColor("TableHeader.background"));
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		ListCellRenderer renderer = getCellRenderer();
		((JLabel) renderer).setHorizontalAlignment(SwingConstants.CENTER);
		setCellRenderer(renderer);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setFont(table.getFont());
		String str = (value == null) ? "" : value.toString();
		BufferedReader br = new BufferedReader(new StringReader(str));
		String line;
		Vector<String> v = new Vector<>();
		try {
			while ((line = br.readLine()) != null) {
				v.addElement(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		setListData(v);
		return this;
	}
}
