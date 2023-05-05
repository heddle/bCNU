package cnuphys.advisors.checklist.steps;

import java.util.List;

import cnuphys.advisors.Advisor;
import cnuphys.advisors.Person;
import cnuphys.advisors.Student;
import cnuphys.advisors.checklist.IAlgorithmStep;
import cnuphys.advisors.enums.Major;
import cnuphys.advisors.model.AdvisorData;
import cnuphys.advisors.model.DataManager;
import cnuphys.advisors.model.StudentData;

/**
 * Assign the honors students
 * @author heddle
 *
 */
public class HonorsStep implements IAlgorithmStep {

	@Override
	public boolean run() {
		// first, assign by major
		// this does not reassign students so only used unassigned


		for (Major major : Major.values()) {

			//get unassigned honors students and honors advisor
			List<Student> students = DataManager.getUnassignedHonorsStudentsForMajor(major);
			List<Advisor> advisors = DataManager.getHonorsAdvisorsForMajor(major, true);
			DataManager.roundRobinAssign(advisors, students, "Honors by Major");

		}

		//now run the algorithm
		AdvisorData advisorData = DataManager.getFilteredAdvisorData(Person.HONOR);
		StudentData studentData = DataManager.getFilteredStudentData(Person.HONOR);

		Algorithm.runAlgorithm(studentData.getStudents(), advisorData.getAdvisors());
		return true;
	}

}
