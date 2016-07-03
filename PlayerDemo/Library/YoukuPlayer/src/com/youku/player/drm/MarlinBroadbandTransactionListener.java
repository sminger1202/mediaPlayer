package com.youku.player.drm;

/*
 * Listener that captures token processing events and saves the license to the
 * License Store
 */

import com.baseproject.utils.Logger;
import com.intertrust.wasabi.ErrorCodeException;
import com.intertrust.wasabi.drm.TransactionListener;
import com.intertrust.wasabi.drm.TransactionType;
import com.intertrust.wasabi.licensestore.LicenseStore;

public class MarlinBroadbandTransactionListener implements TransactionListener {

	static final String TAG = "drm";
	private boolean isSuccessed;

	/*
	 * Saves license to the license store when processing complete
	 */
	public void onLicenseDataReceived(byte[] data) {

		try {
			LicenseStore license_store = new LicenseStore();
			String license = new String(data);
			license_store.addLicense(license, "Marlin License");
			license_store.close();
		} catch (ErrorCodeException x) {
			Logger.e(
					TAG,
					"Failed to create License store: "
							+ x.getLocalizedMessage());
		}

	}

	public void onTransactionEnd(TransactionType transactionType,
			int resultCode, String resultString, String serviceFault) {
		if (resultCode != 0)
			isSuccessed = false;
		else
			isSuccessed = true;
		Logger.d(TAG, "END transaction of type " + transactionType
				+ "\tresult code" + resultCode + "\tresult string "
				+ resultString);

	}

	@Override
	public void onTransactionBegin(TransactionType transactionType) {
		Logger.d(TAG, "BEGIN transaction of type " + transactionType);

	}

	@Override
	public void onTransactionProgress(TransactionType transactionType,
			int arg1, int arg2) {
		Logger.d(TAG, "PROGRESS (notification of) transaction of type "
				+ transactionType);
	}

	public boolean isSuccessed() {
		return isSuccessed;
	}

}
