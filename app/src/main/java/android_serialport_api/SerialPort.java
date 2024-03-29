/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import android.util.Log;

public class SerialPort implements Serializable {
	//防止混淆 native code 需要 mFd字段
	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private final FileDescriptor mFd;
	private final FileInputStream mFileInputStream;
	private final FileOutputStream mFileOutputStream;

	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			/* Missing read/write permission, trying to chmod the file */
			Process su;
			su = Runtime.getRuntime().exec("/system/bin/su");
			String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
					+ "done\n";
			su.getOutputStream().write(cmd.getBytes());

			try {
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException("device can't read or write!");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new SecurityException(e.getMessage());
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags) throws IOException;
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}
}
