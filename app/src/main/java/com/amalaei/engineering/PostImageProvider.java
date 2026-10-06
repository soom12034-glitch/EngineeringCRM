package com.amalaei.engineering;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;
public final class PostImageProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}
    private File file(Uri uri)throws FileNotFoundException {if(!"com.amalaei.engineering.images".equals(uri.getAuthority())||uri.getPathSegments().size()!=1)throw new FileNotFoundException();try{File f=PostMedia.file(getContext(),uri.getLastPathSegment());if(!f.isFile())throw new FileNotFoundException();return f;}catch(IOException e){throw new FileNotFoundException();}}
    @Override public String getType(Uri uri){try{return PostMedia.mime(file(uri).getName());}catch(Exception e){return null;}}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException {if(!"r".equals(mode))throw new FileNotFoundException("القراءة فقط");return ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.MODE_READ_ONLY);}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){try{File f=file(uri);String[] names=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor c=new MatrixCursor(names);Object[] values=new Object[names.length];for(int i=0;i<names.length;i++)values[i]=names[i].equals(OpenableColumns.DISPLAY_NAME)?f.getName():names[i].equals(OpenableColumns.SIZE)?f.length():null;c.addRow(values);return c;}catch(Exception e){return null;}}
    @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
    @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
