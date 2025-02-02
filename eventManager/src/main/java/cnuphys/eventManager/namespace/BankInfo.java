package cnuphys.eventManager.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.jnp.hipo4.data.Schema;

public class BankInfo extends ArrayList<ColumnInfo> implements Comparable<BankInfo>{

	//the bank name
	private String _name;

	//the corresponding schema (bank)
	private Schema _schema;

	//for binary search
	private ColumnInfo _workColumnInfo = new ColumnInfo(null, null, -1);

	//cache column names
	private String[] _columnNames;

	/**
	 * Create a BankInfo object from a schema
	 * @param schema the schema from a DataSource
	 */
	public BankInfo(Schema schema) {

		if (schema == null) {
			_name = "???";
			return;
		}

		_schema = schema;
		_name = _schema.getName();

		//get the columns
		List<String> columns = _schema.getEntryList();
		Collections.sort(columns);

		for (String columnName : columns) {
			int type = schema.getType(columnName);
			add(new ColumnInfo(this, columnName, type));
		}

		Collections.sort(this);
	}

	/**
	 * Get the bank name
	 * @return the bank name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the bank name
	 * @param name the bank name
	 */
	protected void setName(String name) {
		_name = name;
	}

	/**
	 * Get the column using a binary search
	 * @param columnName the name of the column
	 * @return  the column, or null if not found
	 */
	public ColumnInfo getColumnInfo(String columnName) {
		_workColumnInfo.setName(columnName);
		int index = Collections.binarySearch(this, _workColumnInfo);
		if (index >= 0) {
			return get(index);
		}
		else {
			return null;
		}
	}

	/**
	 * Get an array of the column names
	 * @return the column names
	 */
	public String[] getColumnNames() {

		if (_columnNames == null) {
			int len = size();
			_columnNames = new String[len];
			for (int i = 0; i < len; i++) {
				_columnNames[i] = get(i).getName();
			}
		}

		return _columnNames;
	}

	@Override
	public int compareTo(BankInfo o) {
		return _name.compareTo(o._name);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);

		sb.append("Bank [" + _name + "] has " + size() + " columns\n");
		for (ColumnInfo columnInfo : this) {
			sb.append(columnInfo + "\n");
		}

		return sb.toString();
	}

}
