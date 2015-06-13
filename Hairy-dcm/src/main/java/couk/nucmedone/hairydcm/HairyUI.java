package couk.nucmedone.hairydcm;

import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.tool.dcmqr.DcmQR.QueryRetrieveLevel;

public class HairyUI extends Application implements QueryListener {

	public static void main(String[] args) {
		launch(args);
	}

	private final Task<Void> task;

	private final HairyQR hqr;

	private UUID hqrUID;

	private QueryRetrieveLevel level = QueryRetrieveLevel.STUDY;

	private SpecificCharacterSet charSet;

	private TreeItem<String> rootNode = new TreeItem<>("Root");

	public HairyUI() {

		Charset cs = Charset.defaultCharset();
		charSet = new SpecificCharacterSet(cs.toString());

		// make a new query object
		hqr = new HairyQR();
		hqr.addQueryListener(this);
		hqr.setQueryLevel(level);
		hqr.addReturnField(Tag.PatientID);
		hqr.addReturnField(Tag.PatientName);

		// get the queryUID
		hqrUID = hqr.getUUID();

		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				hqr.doQuery();
				return null;
			}
		};

	}

	Button makeButton() {

		Button btn = new Button();
		btn.setText("Search");
		btn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				new Thread(task).start();
			}

		});

		return btn;

	}

	@Override
	public void queryUpdate(List<DicomObject> result, UUID queryUID) {

		// Check UID of query is a match
		if (queryUID.equals(hqrUID)) {
		
			// Add each returned study to the tree
			for (DicomObject dcm : result) {

				StudyTreeNode dtn = new StudyTreeNode(dcm, charSet);
				rootNode.getChildren().add(dtn.getNode());

			}
		}

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		// Study tree
		rootNode.setExpanded(true);
		TreeView<String> tree = new TreeView<>(rootNode);
		tree.setShowRoot(false);

		// Layout
		BorderPane bp = new BorderPane();

		primaryStage.setTitle("Hairy Dicom");
		Button btn = makeButton();
		bp.setTop(btn);
		bp.setCenter(tree);

		primaryStage.setScene(new Scene(bp, 600, 300));
		primaryStage.show();

	}

}
