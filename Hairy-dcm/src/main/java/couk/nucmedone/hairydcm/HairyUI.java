package couk.nucmedone.hairydcm;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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

	private ObservableList<StudyTableModel> studyList;

	private TableView<StudyTableModel> table = new TableView<StudyTableModel>();

	private final Task<Void> task;

	private final HairyQR hqr;

	private QueryRetrieveLevel level = QueryRetrieveLevel.STUDY;

	private SpecificCharacterSet charSet;

	public HairyUI() {

		Charset cs = Charset.defaultCharset();
		charSet = new SpecificCharacterSet(cs.toString());

		hqr = new HairyQR();
		hqr.addQueryListener(this);
		hqr.setQueryLevel(level);
		hqr.addReturnField(Tag.PatientID);
		hqr.addReturnField(Tag.PatientName);

		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				hqr.doQuery();
				return null;
			}
		};

	}

	private void addColumn(String columnName, String columnID) {

		TableColumn<StudyTableModel, String> column = new TableColumn<StudyTableModel, String>(
				columnName);
		column.setCellValueFactory(new PropertyValueFactory<StudyTableModel, String>(
				columnID));
		table.getColumns().add(column);

	}

	private void addStudy(DicomObject dcm) { //HairyStudy hs) {

		StudyTableModel stm = new StudyTableModel(dcm, charSet);

		if (studyList == null) {

			studyList = FXCollections.observableArrayList(stm);
			table.setItems(studyList);

		} else {

			studyList.add(stm);

		}

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
	public void queryUpdate(List<DicomObject> result) {

		Iterator<DicomObject> it = result.iterator();
		while (it.hasNext()) {

			DicomObject dcm = it.next();
			addStudy(dcm);

		}

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		BorderPane bp = new BorderPane();

		primaryStage.setTitle("Hello World!");
		Button btn = makeButton();
		bp.setTop(btn);

		addColumn("Name", StudyTableModel.columnName);
		addColumn("ID", StudyTableModel.columnID);
		addColumn("Accession Number", "accessionNumber");
		addColumn("Study ID", "studyID");
		addColumn("UID", "studyUID");

		bp.setCenter(table);

		primaryStage.setScene(new Scene(bp, 600, 300));
		primaryStage.show();

	}

}
