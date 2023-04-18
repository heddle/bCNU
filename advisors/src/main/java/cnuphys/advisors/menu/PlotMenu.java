package cnuphys.advisors.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.advisors.Advisor;
import cnuphys.advisors.Student;
import cnuphys.advisors.enums.Department;
import cnuphys.advisors.enums.Major;
import cnuphys.advisors.graphics.AdvisorDisplay;
import cnuphys.advisors.graphics.BarPlot;
import cnuphys.advisors.io.ITabled;
import cnuphys.advisors.model.AdvisorData;
import cnuphys.advisors.model.DataManager;
import cnuphys.advisors.model.StudentData;

public class PlotMenu extends JMenu implements ActionListener {

	//the menu items
	private JMenuItem _fcaByDeptItem;
	private JMenuItem _studentByMajorItem;

	
	public PlotMenu() {
		super("Plots");
		MenuManager.getInstance().addMenu(this);
		_fcaByDeptItem = MenuManager.addMenuItem("FCAs by Department", KeyEvent.VK_1, this, this);
		_studentByMajorItem = MenuManager.addMenuItem("Students by Major", KeyEvent.VK_2, this, this);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _fcaByDeptItem) {
			handleFCAByDept();
		}
		else if (e.getSource() == _studentByMajorItem) {
			handleStudentsByMajor();
		}

	}
	
	//handle FCA by department selection
	private void handleFCAByDept() {
		String title = "FCAs by Department";
		
		Department.clearCounts();
		
		AdvisorData advisorData = DataManager.getAdvisorData();

		ArrayList<ITabled> data = advisorData.getData();
		
		for (ITabled itabled : data) {
			Advisor advisor = (Advisor)itabled;
			Department.incrementCount(advisor.department);
		}
		
		String categories[] = Department.getBaseNames();
		int counts[] = Department.getCounts();
		
		BarPlot barPlot = new BarPlot(title, categories, counts);
		AdvisorDisplay.getInstance().setContent(barPlot);
	}
	
	//handle FCA by department selection
	private void handleStudentsByMajor() {
		String title = "Students by Department";
		
		Major.clearCounts();
		
		StudentData studentData = DataManager.getStudentData();

		ArrayList<ITabled> data = studentData.getData();
		
		for (ITabled itabled : data) {
			Student student = (Student)itabled;
			Major.incrementCount(student.major);
		}
		
		String categories[] = Major.getBaseNames();
		int counts[] = Major.getCounts();
		
		BarPlot barPlot = new BarPlot(title, categories, counts);
		AdvisorDisplay.getInstance().setContent(barPlot);
	}



}