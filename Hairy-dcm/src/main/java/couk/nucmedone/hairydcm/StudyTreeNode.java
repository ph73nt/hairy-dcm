package couk.nucmedone.hairydcm;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.tool.dcmqr.DcmQR.QueryRetrieveLevel;

public class StudyTreeNode implements QueryListener {

	private final DicomObject dcm;
	private final SpecificCharacterSet charSet;
	private TreeItem<String> dcmStudy;
	private final HairyQR hqr;
	private final UUID hqrUID;
	private final Task<Void> task;

	public StudyTreeNode(DicomObject dcm, SpecificCharacterSet charSet) {

		this.dcm = dcm;
		this.charSet = charSet;

		// TODO: implement dynamic tree as here:
		// http://www.java2s.com/Tutorials/Java/JavaFX/0660__JavaFX_Tree_View.htm

		hqr = createQR();
		hqrUID = hqr.getUUID();
		task = createTask(hqr);

		StudyTableModel stm = new StudyTableModel(dcm, charSet);

		StringBuilder items = new StringBuilder();
		items.append(stm.getName());
		items.append(" | " + stm.getId());
		items.append(" | " + stm.getAccessionNumber());
		items.append(" | " + stm.getStudyID());
		items.append(" | " + stm.getStudyUID());

		dcmStudy = new TreeItem<String>(items.toString());
		ObservableList<TreeItem<String>> series = FXCollections
				.observableArrayList();
		dcmStudy.getChildren().addAll(series);

		TreeItem<String> dcmSeries = new TreeItem<String>(
				"Searching for matching series...");

		series.add(dcmSeries);

	}

	private HairyQR createQR() {

		HairyQR hairyQR = new HairyQR();

		hairyQR.addQueryListener(this);
		hairyQR.setQueryLevel(QueryRetrieveLevel.SERIES);

		return hairyQR;

	}

	private Task<Void> createTask(HairyQR hairyQR) {

		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				hairyQR.doQuery();
				return null;
			}
		};

	}

	public TreeItem<String> getNode() {

		TreeItem<String> dcmStudy1 = new TreeItem<String>() {

			private boolean isLeaf;
			private boolean isFirstTimeChildren = true;
			private boolean isFirstTimeLeaf = true;

			public ObservableList<TreeItem<String>> getChildren() {

				if (isFirstTimeChildren) {
					isFirstTimeChildren = false;
					super.getChildren().setAll(buildChildren(this));
				}
				return super.getChildren();

			}

			@Override
			public boolean isLeaf() {
				if (isFirstTimeLeaf) {
					isFirstTimeLeaf = false;
				}
				return isLeaf;
			}

			private ObservableList<TreeItem<String>> buildChildren(
					TreeItem<String> TreeItem) {

				ArrayList<TreeItem<String>> list = new ArrayList<>();
				list.add(new TreeItem<String>("One"));
				list.add(new TreeItem<String>("Two"));
				list.add(new TreeItem<String>("Three"));

				ObservableList<TreeItem<String>> children = FXCollections
						.observableArrayList();

				children.add(new TreeItem<String>("One"));
				children.add(new TreeItem<String>("Two"));
				children.add(new TreeItem<String>("Three"));

				return FXCollections.observableList(children);

			}

		};

		StudyTableModel stm = new StudyTableModel(dcm, charSet);

		StringBuilder items = new StringBuilder();
		items.append(stm.getName());
		items.append(" | " + stm.getId());
		items.append(" | " + stm.getAccessionNumber());
		items.append(" | " + stm.getStudyID());
		items.append(" | " + stm.getStudyUID());

		dcmStudy1.setValue(items.toString());

		return dcmStudy1;

	}

	@Override
	public void queryUpdate(List<DicomObject> result, UUID uid) {

		// Check we have a UUID match on the query
		if (uid.equals(hqrUID)) {

			// Do something

		}

	}

}
