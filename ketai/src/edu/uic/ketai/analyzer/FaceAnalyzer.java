package edu.uic.ketai.analyzer;

import processing.core.PApplet;
import processing.core.PVector;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

import edu.uic.ketai.IKetaiEventListener;
import edu.uic.ketai.Ketai;
import edu.uic.ketai.data.DataManager;
import edu.uic.ketai.inputService.KetaiCamera;

public class FaceAnalyzer extends AbstractKetaiAnalyzer {

	final static String NAME = "AllSensorData";
	final static String DESCRIPTION = "Wraps the android face detector function.   Will find a face in the camera preview and save a photo.";
	final static String[] servicesubscription = { "edu.uic.ketai.inputService.KetaiCamera" };
	private static String TABLE_NAME = "facedetected";
	private static String CREATE_TABLE_SQL = "CREATE TABLE facedetected (id INTEGER PRIMARY KEY, timestamp BIGINT, number_of_faces INT)";
	private static String INSERT_SQL = "insert into "
			+ TABLE_NAME
			+ " (timestamp, number_of_faces) values (?, ?)";

	public FaceAnalyzer(DataManager _datamanager) {
		super(_datamanager);
		verifyDatabase();
	}

	public String getAnalysisDescription() {
		return DESCRIPTION;
	}

	public void analyzeData(Object _data) {
		long timestamp = System.currentTimeMillis();

		if (!(_data instanceof KetaiCamera))
			return;
		
		SQLiteStatement insertStatement;
		KetaiCamera data = (KetaiCamera) _data;

		Bitmap _bitmap = Bitmap.createBitmap(data.pixels, data.width, data.height, Bitmap.Config.RGB_565);
		if(_bitmap == null)
		{
			PApplet.println("null bitmap in faceanalyzer");
			return;
		}
		FaceDetector _detector = new FaceDetector(data.width, data.height, 2);
		Face[] faces = new Face[2];
		
		int numberOfFaces = _detector.findFaces(_bitmap, faces);

		if(numberOfFaces < 1)
		{
			broadcastKetaiEvent(Ketai.KETAI_EVENT_NO_FACES_DETECTED, null);			
			return;
		}
		insertStatement = datamanager.getDb().compileStatement(INSERT_SQL);
		insertStatement.bindLong(1, timestamp);
		insertStatement.bindLong(2, numberOfFaces);

		insertStatement.executeInsert();
		PVector _face = new PVector();
		PointF p = new PointF();
		faces[0].getMidPoint(p);
		_face.set(p.x, p.y, 0);
		broadcastKetaiEvent(Ketai.KETAI_EVENT_FACES_DETECTED, _face);
		
		//lets broadcast an event to tell people what we found!
		
	}

	private void broadcastKetaiEvent(int _event, Object _data)
	{
		for(IKetaiEventListener l: ketaiEventListeners){
			l.receiveKetaiEvent(_event, _data);
		}
	}
	
	public String getAnalyzerName() {
		return NAME;
	}

	public String getTableCreationString() {
		return CREATE_TABLE_SQL;
	}

	public Class<?> getServiceProviderClass() {
		try {
			return Class
					.forName("edu.uic.ketai.inputService.KetaiCamera");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void verifyDatabase() {
		if (datamanager.tableExists(TABLE_NAME))
			return;

		// Let's create our table if necessary
		try {
			datamanager.getDb().execSQL(CREATE_TABLE_SQL);
		} catch (SQLiteException sqlx) {
			sqlx.printStackTrace();
		}
	}
	
}

