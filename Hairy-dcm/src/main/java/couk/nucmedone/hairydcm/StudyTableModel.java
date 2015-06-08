package couk.nucmedone.hairydcm;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;

import javafx.beans.property.SimpleStringProperty;

public class StudyTableModel {

	public static final String columnID = "id";
	public static final String columnName = "name";
	public static final String columnAccNum = "accessionNumber";
	public static final String columnStudyID = "studyID";
	public static final String columnStudyUID = "studyUID";

	private SimpleDicomProperty accessionNumber;
	private SimpleDicomProperty id;
	private SimpleDicomProperty name;
	private SimpleDicomProperty studyDate;
	private SimpleDicomProperty studyID;
	private SimpleDicomProperty studyUID;

	public final DicomObject study;
	private final SpecificCharacterSet cs;

	class SimpleDicomProperty extends SimpleStringProperty {

		private DicomElement de;
		private final SpecificCharacterSet cs;

		public SimpleDicomProperty(DicomElement initialValue,
				SpecificCharacterSet cs) {
			this.cs = cs;
			set(initialValue);
		}

		public DicomElement getDicomElement() {
			return de;
		}

		public void set(DicomElement dicomElement) {
			de = dicomElement;
			set(de.getString(cs, false));
		}

	}

	public StudyTableModel(DicomObject study, SpecificCharacterSet charSet) {

		this.study = study;
		cs = charSet;
		accessionNumber = getDcmProp(Tag.AccessionNumber);
		id = getDcmProp(Tag.PatientID);
		name = getDcmProp(Tag.PatientName);
		studyDate = getDcmProp(Tag.StudyDate);
		studyID = getDcmProp(Tag.StudyDate);
		studyUID = getDcmProp(Tag.StudyInstanceUID);

	}

	private SimpleDicomProperty getDcmProp(int tag) {

		DicomElement de = study.get(tag);
		if (de == null) {

			return null;

		} else {

			return new SimpleDicomProperty(de, cs);

		}

	}

	public String getAccessionNumber() {
		return accessionNumber == null ? null : accessionNumber.get();
	}

	public String getId() {
		return id == null ? null : id.get();
	}

	public String getName() {
		return name == null ? null : name.get();
	}

	public String getStudyDate() {
		return studyDate == null ? null : studyDate.get();
	}

	public String getStudyID() {
		return studyID == null ? null : studyID.get();
	}

	public String getStudyUID() {
		return studyUID == null ? null : studyUID.get();
	}

}
