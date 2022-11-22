package cnuphys.simanneal.advisors.model;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.event.ListSelectionEvent;

import cnuphys.simanneal.advisors.Advisor;
import cnuphys.simanneal.advisors.AdvisorAssign;
import cnuphys.simanneal.advisors.io.DataModel;
import cnuphys.simanneal.advisors.io.ITabled;
import cnuphys.simanneal.advisors.table.InputOutput;

/**
 * Contains all the advisor data
 * @author heddle
 *
 */
public class AdvisorData extends DataModel {
	
	//attributes for advisor data
	private static final DataAttribute advisorAttributes[] = {DataManager.lastNameAtt, 
			DataManager.firstNameAtt, DataManager.departmentNameAtt};
	
	public AdvisorData(String baseName) {
		super(baseName, advisorAttributes);
	}
	

	@Override
	protected void processData() {
		int colCount = _header.length;
		InputOutput.debugPrintln("ADVISOR row count: " + _data.size());
		InputOutput.debugPrintln("ADVISOR col count: " + colCount);
		
		int lastIndex = getColumnIndex(DataManager.lastNameAtt);
		int firstIndex = getColumnIndex(DataManager.firstNameAtt);
		int deptIndex = getColumnIndex(DataManager.departmentNameAtt);
		
		
		InputOutput.debugPrintln(String.format("Column Indices (last, first, depart) = (%d, %d, %d)", lastIndex, firstIndex, deptIndex));
		
		for (String s[] : _data) {
			String lastName = s[lastIndex];
			String firstName = s[firstIndex];
			String dept = s[deptIndex];
			
			_tableData.add(new Advisor(lastName, firstName, dept));
		}
		
		//raw data not needed
		deleteRawData();
		
		if (AdvisorAssign.DEBUG) {
			int i = 0;
			for (ITabled itabled : _tableData) {
				Advisor advisor = (Advisor) itabled;
				String s = String.format("%-4d %s", (++i), advisor.nameAndDepartment());
				System.out.println(s);
			}
		}
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			System.err.println("Selected Rows: ");
				
			for (int row : getSelectedRows()) {
				System.err.println("  " + row);
			}
			
			System.err.println();
		}
	}


}
