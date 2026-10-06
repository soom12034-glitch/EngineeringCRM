package com.amalaei.engineering;
import android.content.*;import android.database.*;import android.net.Uri;import android.os.ParcelFileDescriptor;import android.provider.OpenableColumns;import java.io.*;
public final class QuoteProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 private File file(Uri u)throws FileNotFoundException{if(!"com.amalaei.engineering.quotes".equals(u.getAuthority())||u.getPathSegments().size()!=1||!u.getLastPathSegment().matches("quote-[a-f0-9]{32}\\.pdf"))throw new FileNotFoundException();File f=new File(getContext().getCacheDir(),u.getLastPathSegment());if(!f.isFile())throw new FileNotFoundException();return f;}
 public String getType(Uri u){return "application/pdf";}
 public ParcelFileDescriptor openFile(Uri u,String mode)throws FileNotFoundException{if(!mode.equals("r"))throw new FileNotFoundException();return ParcelFileDescriptor.open(file(u),ParcelFileDescriptor.MODE_READ_ONLY);}
 public Cursor query(Uri u,String[] projection,String s,String[] a,String order){try{File f=file(u);MatrixCursor c=new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE});c.addRow(new Object[]{f.getName(),f.length()});return c;}catch(Exception e){return null;}}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
