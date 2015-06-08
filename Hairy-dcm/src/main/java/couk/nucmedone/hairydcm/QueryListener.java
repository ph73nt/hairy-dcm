package couk.nucmedone.hairydcm;

import java.util.List;

import org.dcm4che2.data.DicomObject;

public interface QueryListener {

	public void queryUpdate(List<DicomObject> result);
	
}
