package couk.nucmedone.hairydcm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.tool.dcmqr.DcmQR;
import org.dcm4che2.tool.dcmqr.DcmQR.QueryRetrieveLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HairyQR {

	private static final Logger LOG = LoggerFactory.getLogger(DcmQR.class);
	
	/**
	 * Uniquely identify this query
	 */
	private final UUID uuid;
	
	public static void main(String[] args) {

		HairyQR hqr = new HairyQR();
		hqr.doQuery();

	}
	public final String calledAE = "DCM4CHEE";
	public final String calledIP = "127.0.0.1";
	public final int calledPort = 11112;
	public final String callingAET = "STORESCU";

	private final DcmQR dcmqr;

	private QueryListener queryListener = null;
	
	private QueryRetrieveLevel level = QueryRetrieveLevel.STUDY;
	
	public HairyQR() {
		dcmqr = new DcmQR("Device");
		uuid = UUID.randomUUID();
	}
	
	public HairyQR(String name){
		dcmqr = new DcmQR(name);
		uuid = UUID.randomUUID();
	}
	
	public void addQueryField(int tag, String match){
	
		int[] tags = {tag};
		dcmqr.addMatchingKey(tags, match);
	}
	
	public void addQueryListener(QueryListener ql) {
		this.queryListener = ql;
	}
	
	public void addReturnField(int tag){
		int[] tags = {tag};
		dcmqr.addReturnKey(tags);
	}

	public void doQuery() {

		dcmqr.setQueryLevel(level);
		dcmqr.setCalledAET(calledAE, true);
		dcmqr.setRemoteHost(calledIP);
		dcmqr.setRemotePort(calledPort);
		dcmqr.setCalling(callingAET);
		dcmqr.setCFind(true);

		dcmqr.setPackPDV(false);
		dcmqr.setTcpNoDelay(false);
		dcmqr.setMaxOpsInvoked(1);
		dcmqr.setMaxOpsPerformed(0);

		dcmqr.setCGet(false);
		dcmqr.configureTransferCapability(false);
		dcmqr.addDefReturnKeys();
		

		int repeat = 0;
		int interval = 0;
		boolean closeAssoc = false;

		try {
			dcmqr.start();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}

		try {
			long t1 = System.currentTimeMillis();
			try {
				dcmqr.open();
			} catch (Exception e) {
				LOG.error("Failed to establish association:", e);
				System.exit(2);
			}
			long t2 = System.currentTimeMillis();
			LOG.info("Connected to {} in {} s", calledAE,
					Float.valueOf((t2 - t1) / 1000f));

			for (;;) {
				List<DicomObject> result;
				if (dcmqr.isCFind()) {
					result = dcmqr.query();
					queryUpdate(result);
					long t3 = System.currentTimeMillis();
					LOG.info("Received {} matching entries in {} s",
							Integer.valueOf(result.size()),
							Float.valueOf((t3 - t2) / 1000f));
					t2 = t3;
				} else {
					result = Collections.singletonList(dcmqr.getKeys());
					queryUpdate(result);
				}
				if (dcmqr.isCMove() || dcmqr.isCGet()) {
					if (dcmqr.isCMove())
						dcmqr.move(result);
					else
						dcmqr.get(result);
					long t4 = System.currentTimeMillis();
					LOG.info(
							"Retrieved {} objects (warning: {}, failed: {}) in {}s",
							new Object[] {
									Integer.valueOf(dcmqr.getTotalRetrieved()),
									Integer.valueOf(dcmqr.getWarning()),
									Integer.valueOf(dcmqr.getFailed()),
									Float.valueOf((t4 - t2) / 1000f) });
				}
				if (repeat == 0 || closeAssoc) {
					try {
						dcmqr.close();
					} catch (InterruptedException e) {
						LOG.error(e.getMessage(), e);
					}
					LOG.info("Released connection to {}", calledAE);
				}
				if (repeat-- == 0)
					break;
				Thread.sleep(interval);
				long t4 = System.currentTimeMillis();
				dcmqr.open();
				t2 = System.currentTimeMillis();
				LOG.info("Reconnect or reuse connection to {} in {} s",
						calledAE, Float.valueOf((t2 - t4) / 1000f));
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		} catch (ConfigurationException e) {
			LOG.error(e.getMessage(), e);
		} catch (RuntimeException e){
			LOG.error(e.getMessage(), e);
		} finally {
			dcmqr.stop();
		}

	}
	
	public UUID getUUID(){
		return uuid;
	}

	private void queryUpdate(List<DicomObject> result) {

		if (queryListener != null) {
			queryListener.queryUpdate(result, uuid);
		}

	}

	public void setQueryLevel(QueryRetrieveLevel level){
		this.level = level;
	}
}
