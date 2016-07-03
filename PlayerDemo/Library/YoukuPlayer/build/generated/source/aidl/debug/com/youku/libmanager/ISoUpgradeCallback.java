/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\PlayerDemo\\Library\\YoukuPlayer\\src\\com\\youku\\libmanager\\ISoUpgradeCallback.aidl
 */
package com.youku.libmanager;
// Declare any non-default types here with import statements

public interface ISoUpgradeCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.youku.libmanager.ISoUpgradeCallback
{
private static final java.lang.String DESCRIPTOR = "com.youku.libmanager.ISoUpgradeCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.youku.libmanager.ISoUpgradeCallback interface,
 * generating a proxy if needed.
 */
public static com.youku.libmanager.ISoUpgradeCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.youku.libmanager.ISoUpgradeCallback))) {
return ((com.youku.libmanager.ISoUpgradeCallback)iin);
}
return new com.youku.libmanager.ISoUpgradeCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onDownloadEnd:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onDownloadEnd(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onDownloadFailed:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onDownloadFailed(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.youku.libmanager.ISoUpgradeCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
@Override public void onDownloadEnd(java.lang.String soName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(soName);
mRemote.transact(Stub.TRANSACTION_onDownloadEnd, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onDownloadFailed(java.lang.String soName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(soName);
mRemote.transact(Stub.TRANSACTION_onDownloadFailed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onDownloadEnd = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDownloadFailed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void onDownloadEnd(java.lang.String soName) throws android.os.RemoteException;
public void onDownloadFailed(java.lang.String soName) throws android.os.RemoteException;
}
